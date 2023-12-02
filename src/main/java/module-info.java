module br.ufrn.imd.player {
    requires javafx.controls;
    requires javafx.fxml;
    requires com.fasterxml.jackson.databind;
    requires com.google.gson;
    requires javafx.media;
    requires java.base;
    requires MaterialFX;

    opens br.ufrn.imd.player.main to javafx.fxml;
    exports br.ufrn.imd.player.main;
    exports br.ufrn.imd.player.ControlAuth;
    exports br.ufrn.imd.player.ControlMain;
    exports br.ufrn.imd.player.models;

    opens br.ufrn.imd.player.ControlAuth to javafx.fxml, com.google.gson; // Adicione esta linha para abrir o pacote
    opens br.ufrn.imd.player.ControlMain to javafx.fxml, com.google.gson;
    opens br.ufrn.imd.player.models to com.google.gson;

}
