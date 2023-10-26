module com.aus.prva_semestralka {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.web;

    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires net.synedra.validatorfx;
    requires org.kordamp.ikonli.javafx;
    requires org.kordamp.bootstrapfx.core;
    requires com.almasb.fxgl.all;
	requires lombok;
	requires java.logging;

	opens com.aus.prva_semestralka to javafx.fxml;
    exports com.aus.prva_semestralka;
    exports com.aus.prva_semestralka.objekty;
	exports com.aus.prva_semestralka.struktury;
}