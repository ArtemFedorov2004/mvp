package io.github.artemfedorov2004.managerapp.entity;

import java.math.BigDecimal;

public record Product(Long id, String title, BigDecimal price) {
}
