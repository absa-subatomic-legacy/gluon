package za.co.absa.subatomic.domain.application;

import za.co.absa.subatomic.domain.member.TeamMember;

public interface ApplicationBase {

    String getApplicationId();

    String getName();

    String getDescription();

    ApplicationType getApplicationType();

    TeamMember getCreatedBy();

}
