package com.esw.parkingmanager.dto;

import java.util.List;

public record BulkSpotRequest(List<String> labels) {
}