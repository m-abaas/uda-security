module com.udacity.security {
    requires java.desktop;
    requires com.google.common;
    requires java.prefs;
    requires com.google.gson;
    requires com.miglayout.swing;
    requires com.udacity.image;
    opens com.udacity.security.service to com.udacity.image;
}