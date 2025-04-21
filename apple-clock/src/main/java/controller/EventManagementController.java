package controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import model.WorkType;
import service.WorkTypeService;

import java.util.ArrayList;
import java.util.List;

public class EventManagementController {

    @FXML
    private VBox workTypeList;
    @FXML
    private Button addButton;
    @FXML
    private Button deleteButton;
    @FXML
    private Button cancelButton;

    @FXML
    private Button backButton;

    private final ObservableList<WorkType> workTypes = FXCollections.observableArrayList();
    private final List<Long> selectedIds = new ArrayList<>();

    private WorkTypeService workTypeService;

    public void setWorkTypeService(WorkTypeService service) {
        this.workTypeService = service;
        refreshList();
    }

    private void refreshList() {
        workTypeList.getChildren().clear();
        workTypes.clear();

        List<WorkType> types = workTypeService.getAllTypes();
        workTypes.addAll(types);

        for (WorkType type : types) {
            AnchorPane card = createTypeCard(type);
            workTypeList.getChildren().add(card);
        }

        checkAddButtonStatus();
        updateActionButtonsVisibility();
    }

    private AnchorPane createTypeCard(WorkType type) {
        AnchorPane card = new AnchorPane();
        card.setPrefHeight(50);
        card.setStyle("-fx-background-radius: 12; -fx-border-radius: 12; -fx-background-color: white; -fx-border-color: #E0E0E0; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 5, 0, 0, 2);");

        CheckBox selectBox = new CheckBox();
        selectBox.setLayoutX(10);
        selectBox.setLayoutY(15);
        selectBox.addEventHandler(MouseEvent.MOUSE_CLICKED, event -> onTypeSelected(type.getId(), selectBox.isSelected()));

        TextField nameField = new TextField(type.getName());
        nameField.setLayoutX(50);
        nameField.setLayoutY(10);
        nameField.setPrefWidth(300);
        nameField.setPromptText("输入名称 (20字符以内)");
        nameField.textProperty().addListener((obs, oldText, newText) -> {
            if (newText.length() > 20) {
                nameField.setText(oldText);
            }
        });

        card.getChildren().addAll(selectBox, nameField);
        card.setUserData(new CardData(type.getId(), nameField));

        return card;
    }

    private void onTypeSelected(Long id, boolean isSelected) {
        if (isSelected) {
            selectedIds.add(id);
        } else {
            selectedIds.remove(id);
        }
        updateActionButtonsVisibility();
    }

    private void updateActionButtonsVisibility() {
        boolean hasSelection = !selectedIds.isEmpty();
        deleteButton.setVisible(hasSelection);
        cancelButton.setVisible(hasSelection);
    }

    private void checkAddButtonStatus() {
        addButton.setDisable(workTypes.size() >= 15);
    }

    @FXML
    private void onAddClicked() {
        if (workTypes.size() >= 15) {
            showAlert("最多只能创建15个事件类型！");
            return;
        }
        workTypeService.addType(new WorkType("新事件"));
        refreshList();
    }

    @FXML
    private void onSaveClicked() {
        boolean hasError = false;

        for (var node : workTypeList.getChildren()) {
            if (node instanceof AnchorPane card
                    && card.getUserData() instanceof CardData data) {

                String newName = data.nameField.getText().trim();
                if (!newName.isEmpty()) {

                    // ★ 直接用 id + name 调 update
                    WorkType updated = new WorkType();
                    updated.setId(data.id);
                    updated.setName(newName);

                    if (workTypeService.update(updated) == null) {
                        hasError = true;
                        break;
                    }
                }
            }
        }

        if (hasError) {
            showAlert("保存失败：存在重复的事件名称，请检查后重新保存！");
        } else {
            showAlert("保存成功！");
            refreshList();
        }
    }


    @FXML
    private void onDeleteClicked() {
        if (selectedIds.isEmpty()) return;

        workTypeService.deleteType(selectedIds);
        selectedIds.clear();
        refreshList();
    }

    @FXML
    private void onCancelClicked() {
        selectedIds.clear();
        refreshList();
    }

    private void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("提示");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    static class CardData {
        Long id;
        TextField nameField;

        public CardData(Long id, TextField nameField) {
            this.id = id;
            this.nameField = nameField;
        }
    }
    @FXML
    private void handleBack() {
        Stage stage = (Stage) backButton.getScene().getWindow();
        stage.close();
    }


}
