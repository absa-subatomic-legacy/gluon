package za.co.absa.subatomic.domain.team;

import com.fasterxml.jackson.annotation.JsonCreator;

public enum MembershipRequestStatus {

    OPEN("OPEN"),
    REJECTED("REJECTED"),
    APPROVED("APPROVED");

    private String text;

    MembershipRequestStatus(String text) {
        this.text = text;
    }

    @Override
    public String toString() {
        return this.text;
    }

    @JsonCreator
    public static MembershipRequestStatus fromText(String text) {
        for (MembershipRequestStatus status : MembershipRequestStatus
                .values()) {
            if (status.toString().equals(text)) {
                return status;
            }
        }
        throw new IllegalArgumentException();
    }
}
