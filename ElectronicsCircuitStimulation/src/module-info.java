module application {
	requires transitive java.desktop;
    requires javafx.controls;
    requires javafx.fxml;
    exports main;
    exports components;
    exports controller;
    exports view;
    exports model;
}