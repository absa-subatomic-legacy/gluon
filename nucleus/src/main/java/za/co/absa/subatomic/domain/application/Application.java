package za.co.absa.subatomic.domain.application;

import za.co.absa.subatomic.adapter.application.rest.BitbucketRepository;

public interface Application extends ApplicationBase {

    String getProjectId();

    BitbucketRepository getBitbucketRepository();

}
