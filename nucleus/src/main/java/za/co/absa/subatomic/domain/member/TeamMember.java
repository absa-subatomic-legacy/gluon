package za.co.absa.subatomic.domain.member;

public interface TeamMember {

    String getMemberId();

    String getFirstName();

    String getLastName();

    String getEmail();

    String getDomainUsername();

    TeamMemberSlack getSlack();

}
