package com.saas.smartcampus.fee.entity;

import com.saas.smartcampus.shared.entity.AbstractTenantEntity;
import jakarta.persistence.*;
import org.hibernate.annotations.Filter;
import org.hibernate.annotations.FilterDef;
import org.hibernate.annotations.ParamDef;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "fee_structures")
@FilterDef(
    name = "tenantFilter",
    parameters = @ParamDef(name = "tenantId", type = String.class)
)
@Filter(
    name = "tenantFilter",
    condition = "tenant_id = :tenantId"
)
public class FeeStructure extends AbstractTenantEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name; // e.g. Tuition Fee - FY 2026

    @Column(nullable = false)
    private BigDecimal amount;

    @Column(name = "due_date", nullable = false)
    private LocalDate dueDate;

    // Constructors
    public FeeStructure() {}

    public FeeStructure(Long id, String name, BigDecimal amount, LocalDate dueDate) {
        this.id = id;
        this.name = name;
        this.amount = amount != null ? amount : BigDecimal.ZERO;
        this.dueDate = dueDate;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public LocalDate getDueDate() { return dueDate; }
    public void setDueDate(LocalDate dueDate) { this.dueDate = dueDate; }

    // Builder
    public static FeeStructureBuilder builder() {
        return new FeeStructureBuilder();
    }

    public static class FeeStructureBuilder {
        private Long id;
        private String name;
        private BigDecimal amount;
        private LocalDate dueDate;
        private String tenantId;

        public FeeStructureBuilder id(Long id) { this.id = id; return this; }
        public FeeStructureBuilder name(String name) { this.name = name; return this; }
        public FeeStructureBuilder amount(BigDecimal amount) { this.amount = amount; return this; }
        public FeeStructureBuilder dueDate(LocalDate dueDate) { this.dueDate = dueDate; return this; }
        public FeeStructureBuilder tenantId(String tenantId) { this.tenantId = tenantId; return this; }

        public FeeStructure build() {
            FeeStructure feeStructure = new FeeStructure(id, name, amount, dueDate);
            if (tenantId != null) {
                feeStructure.setTenantId(tenantId);
            }
            return feeStructure;
        }
    }
}
