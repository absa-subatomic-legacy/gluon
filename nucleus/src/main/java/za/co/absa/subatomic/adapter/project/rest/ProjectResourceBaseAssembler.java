package za.co.absa.subatomic.adapter.project.rest;

import org.springframework.hateoas.mvc.ResourceAssemblerSupport;

import za.co.absa.subatomic.infrastructure.project.view.jpa.ProjectEntity;

public class ProjectResourceBaseAssembler extends
        ResourceAssemblerSupport<ProjectEntity, ProjectResourceBase> {

    public ProjectResourceBaseAssembler() {
        super(ProjectController.class, ProjectResourceBase.class);
    }

    @Override
    public ProjectResourceBase toResource(ProjectEntity entity) {
        if (entity != null) {
            ProjectResourceBase resource = createResourceWithId(
                    entity.getProjectId(), entity);
            resource.setProjectId(entity.getProjectId());
            resource.setDescription(entity.getDescription());
            resource.setName(entity.getName());
            resource.setOwningTenant(entity.getOwningTenant().getTenantId());

            return resource;
        }
        return null;
    }
}