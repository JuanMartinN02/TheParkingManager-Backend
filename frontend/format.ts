// Every property runs on Houston time (same rule as the backend's Clock). One place to change it
const APP_TIME_ZONE = 'America/Chicago';

// "2026-12-31" is a calendar date with no time. new Date("2026-12-31") would read it as UTC midnight,
// which in Houston is still Dec 30. So it's built from its parts and printed in UTC, where nothing shifts
export function formatDate(isoDate: string): string {
  const [year, month, day] = isoDate.split('-').map(Number);
  return new Intl.DateTimeFormat('en-US', { month: 'short', day: 'numeric', year: 'numeric', timeZone: 'UTC' })
    .format(new Date(Date.UTC(year, month - 1, day)));
}

// A moment in time, like checkedAt, shown as the clock reads in Houston
export function formatTime(isoDateTime: string): string {
  return new Intl.DateTimeFormat('en-US', { hour: 'numeric', minute: '2-digit', timeZone: APP_TIME_ZONE })
    .format(new Date(isoDateTime));
}