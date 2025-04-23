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
import util.BaseController;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import util.I18nKey;
import util.LocalizationManager;

public class EventManagementController extends BaseController{

    @FXML
    private VBox workTypeList;
    @FXML @I18nKey("event.add")
    private Button addButton;
    @FXML @I18nKey("event.deleteSelected")
    private Button deleteButton;
    @FXML @I18nKey("button.cancel")
    private Button cancelButton;

    @FXML @I18nKey("button.back")
    private Button backButton;
    @FXML private HBox bottomActionBar;
    @FXML @I18nKey("button.save")  private Button saveButton;

    private final ObservableList<WorkType> workTypes = FXCollections.observableArrayList();
    private final List<Long> selectedIds = new ArrayList<>();

    private WorkTypeService workTypeService;

    @Override
    protected void onInitialize(URL location, ResourceBundle resources) {
        // 确保第一页加载时能根据 selectedIds 正确显示
        updateActionButtonsVisibility();
    }

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
        nameField.setPromptText(LocalizationManager.getBundle().getString("event.namePrompt"));
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

        // 原来只切子按钮
        deleteButton.setVisible(hasSelection);
        cancelButton.setVisible(hasSelection);

        // 现在让整条底部横条出现／消失
        bottomActionBar.setVisible(hasSelection);
        bottomActionBar.setManaged(hasSelection);

        // 没选中时，只显示「保存」；选中时隐藏「保存」
        saveButton.setVisible(!hasSelection);
        saveButton.setManaged(!hasSelection);
    }

    private void checkAddButtonStatus() {
        addButton.setDisable(workTypes.size() >= 15);
    }

    @FXML
    private void onAddClicked() {
        if (workTypes.size() >= 15) {
            showAlert(LocalizationManager.getBundle().getString("event.addLimit"));
            return;
        }
        workTypeService.addType(new WorkType(LocalizationManager.getBundle().getString("event.newItem") ));
        refreshList();
    }

    @FXML
    private void onSaveClicked() {
        boolean error = false;
        for (var node : workTypeList.getChildren()) {
            if (node instanceof AnchorPane card && card.getUserData() instanceof CardData data) {
                String newName = data.nameField.getText().trim();
                if (!newName.isEmpty()) {
                    WorkType updated = new WorkType();
                    updated.setId(data.id);
                    updated.setName(newName);
                    if (workTypeService.update(updated) == null) {
                        error = true;
                        break;
                    }
                }
            }
        }
        String key = error ? "event.saveFail" : "event.saveSuccess";
        showAlert(LocalizationManager.getBundle().getString(key)); // ★ i18n alert
        if (!error) refreshList();
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
