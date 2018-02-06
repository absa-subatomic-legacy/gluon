package za.co.absa.subatomic.domain.pkg;

import lombok.Getter;

@Getter
public enum PackageType {

    SHARED_LIBRARY("Shared Library"),
    APPLICATION("Application");

    private String name;

    PackageType(String name) {
        this.name = name;
    }
}
