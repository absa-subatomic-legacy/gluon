package za.co.absa.subatomic.domain.application;

import za.co.absa.subatomic.adapter.application.rest.BitbucketRepository;

public interface ApplicationInterface extends ApplicationBaseInterface {

    String getProjectId();

    BitbucketRepository getBitbucketRepository();

}
