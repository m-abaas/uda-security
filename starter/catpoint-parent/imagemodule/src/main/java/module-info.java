module com.udacity.image {
    requires software.amazon.awssdk.services.rekognition;
    requires software.amazon.awssdk.regions;
    requires software.amazon.awssdk.core;
    requires software.amazon.awssdk.auth;
    requires org.slf4j;
    requires java.desktop;
    exports com.udacity.image.service;
}