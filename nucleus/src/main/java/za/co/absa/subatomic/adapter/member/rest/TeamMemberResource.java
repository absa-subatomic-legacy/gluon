package za.co.absa.subatomic.adapter.member.rest;

import java.util.ArrayList;
import java.util.List;

import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class TeamMemberResource extends TeamMemberResourceBase {

    private List<TeamResource> teams = new ArrayList<>();
}
