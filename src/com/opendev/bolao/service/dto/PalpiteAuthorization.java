package com.opendev.bolao.service.dto;

import java.util.Objects;

public final class PalpiteAuthorization {

    public enum Status {
        PENDING("pending"),
        REGISTERED("registered"),
        LOCKED("locked");

        private final String key;

        Status(String key) {
            this.key = key;
        }

        public String getKey() {
            return key;
        }
    }

    public enum RejectionReason {
        NONE(null),
        TIME_WINDOW("timeWindow"),
        ROLE_MISSING("roleMissing"),
        ADMIN_RESTRICTED("adminRestricted"),
        UNKNOWN("unknown");

        private final String key;

        RejectionReason(String key) {
            this.key = key;
        }

        public String getKey() {
            return key;
        }
    }

    private final boolean permitido;
    private final Status status;
    private final RejectionReason reason;

    private PalpiteAuthorization(boolean permitido, Status status, RejectionReason reason) {
        this.permitido = permitido;
        this.status = Objects.requireNonNull(status, "status");
        this.reason = Objects.requireNonNull(reason, "reason");
    }

    public static PalpiteAuthorization permitido(Status status) {
        return new PalpiteAuthorization(true, status, RejectionReason.NONE);
    }

    public static PalpiteAuthorization negado(Status status, RejectionReason reason) {
        return new PalpiteAuthorization(false, status, reason);
    }

    public boolean isPermitido() {
        return permitido;
    }

    public Status getStatus() {
        return status;
    }

    public RejectionReason getReason() {
        return reason;
    }
}
