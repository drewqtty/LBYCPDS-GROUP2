module lbycpds.group2.lbycpds2 {
    requires javafx.controls;
    requires javafx.fxml;


    opens lbycpds.group2 to javafx.fxml;
    exports lbycpds.group2;
}