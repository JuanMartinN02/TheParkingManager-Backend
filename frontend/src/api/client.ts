// The only place in the app that calls fetch. Every screen goes through here
// frontend’s equivalent of GlobalExceptionHandler plus SecurityConfig

// What the backend sends on any error (RFC 9457 ProblemDetail)
type ProblemDetail = {
  status?: number;
  detail?: string;
  errors?: Record<string, string>;
};

// An error a screen can show: one message, plus a message per form field when the backend sent them
export class ApiError extends Error {
  status: number;
  fieldErrors: Record<string, string>;

  constructor(status: number, message: string, fieldErrors: Record<string, string> = {}) {
    super(message);
    this.status = status;
    this.fieldErrors = fieldErrors;
  }
}

type Method = 'GET' | 'POST' | 'PUT' | 'DELETE';

type RequestOptions = {
  body?: unknown;
  form?: URLSearchParams;
  // At login a 401 means "wrong password", not "session expired"
  isLogin?: boolean;
};

let csrfToken: string | null = null;

// The session context decides what these do (go to sign in, go to change password)
let onUnauthorized: () => void = () => {};
let onPasswordChangeRequired: () => void = () => {};

export function setAuthHandlers(handlers: { onUnauthorized: () => void; onPasswordChangeRequired: () => void }) {
  onUnauthorized = handlers.onUnauthorized;
  onPasswordChangeRequired = handlers.onPasswordChangeRequired;
}

// Spring changes the token at login and logout, so the old one has to be dropped
export function forgetCsrfToken() {
  csrfToken = null;
}

async function getCsrfToken(): Promise<string> {
  if (csrfToken) return csrfToken;
  const response = await fetch('/api/auth/csrf', { credentials: 'same-origin' });
  if (!response.ok) throw new ApiError(response.status, "Can't reach the server. Check your connection and try again");
  const body = (await response.json()) as { token: string };
  csrfToken = body.token;
  return csrfToken;
}

export async function request<T>(method: Method, path: string, options: RequestOptions = {}, isRetry = false): Promise<T> {
  const headers: Record<string, string> = {};
  let payload: BodyInit | undefined;

  // Spring rejects any POST/PUT/DELETE without this header, login included
  if (method !== 'GET') headers['X-CSRF-TOKEN'] = await getCsrfToken();

  if (options.form) {
    headers['Content-Type'] = 'application/x-www-form-urlencoded';
    payload = options.form;
  } else if (options.body !== undefined) {
    headers['Content-Type'] = 'application/json';
    payload = JSON.stringify(options.body);
  }

  const response = await fetch('/api' + path, { method, headers, body: payload, credentials: 'same-origin' });

  if (response.ok) {
    // 204 No Content has no body to read
    if (response.status === 204) return undefined as T;
    return (await response.json()) as T;
  }

  const problem = await readProblem(response);

  // A 403 with no body comes from the CSRF check (e.g. the session expired). Retry once with a fresh token
  if (response.status === 403 && !problem && method !== 'GET' && !isRetry) {
    forgetCsrfToken();
    return request<T>(method, path, options, true);
  }

  if (response.status === 401) {
    if (options.isLogin) throw new ApiError(401, 'Wrong email or password');
    onUnauthorized();
    throw new ApiError(401, 'Your session ended. Sign in again');
  }

  if (response.status === 403 && problem?.detail === 'Password change required') {
    onPasswordChangeRequired();
  }

  throw new ApiError(response.status, problem?.detail ?? defaultMessage(response.status), problem?.errors ?? {});
}

async function readProblem(response: Response): Promise<ProblemDetail | null> {
  if (!response.headers.get('Content-Type')?.includes('json')) return null;
  try {
    return (await response.json()) as ProblemDetail;
  } catch {
    return null;
  }
}

function defaultMessage(status: number): string {
  if (status === 403) return "You don't have access to this";
  if (status === 404) return "This doesn't exist anymore";
  if (status >= 500) return 'Something went wrong on the server. Try again or contact support';
  return 'The request could not be completed';
}

export const api = {
  get: <T>(path: string) => request<T>('GET', path),
  post: <T>(path: string, body?: unknown) => request<T>('POST', path, { body }),
  put: <T>(path: string, body?: unknown) => request<T>('PUT', path, { body }),
  delete: <T>(path: string) => request<T>('DELETE', path),
};