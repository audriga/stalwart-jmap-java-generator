package com.audriga.stalwartgenerator.schema;

import java.util.List;

public record StalwartDashboard(String id, String label, List<StalwartCard> cards, List<StalwartChart> charts) {}
