package org.code4everything.wetool.plugin.dbops.controller;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.exceptions.ExceptionUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.thread.ThreadUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.parser.Feature;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import lombok.extern.slf4j.Slf4j;
import org.code4everything.wetool.plugin.dbops.ScriptExecutor;
import org.code4everything.wetool.plugin.dbops.WetoolSupporter;
import org.code4everything.wetool.plugin.dbops.script.ExecuteTypeEnum;
import org.code4everything.wetool.plugin.dbops.script.QlScript;
import org.code4everything.wetool.plugin.support.BaseViewController;
import org.code4everything.wetool.plugin.support.druid.DruidSource;
import org.code4everything.wetool.plugin.support.event.EventCenter;
import org.code4everything.wetool.plugin.support.factory.BeanFactory;
import org.code4everything.wetool.plugin.support.util.DialogWinnable;
import org.code4everything.wetool.plugin.support.util.FxDialogs;
import org.code4everything.wetool.plugin.support.util.FxUtils;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * @author pantao
 * @since 2020/11/11
 */
@Slf4j
public class MainController implements BaseViewController {

    public static final String TAB_ID = "ease-db-ops";

    public static final String TAB_NAME = "数据库小应用";

    private static final JSONObject SCRIPTS = new JSONObject(true);

    private static final String HOME_PATH = FileUtil.getUserHomePath();

    private static final File DB_OPS_PATH = FileUtil.file(HOME_PATH, "wetool", "wetool-plugin-dbops", ".dbops");

    private static final File SCRIPT_JSON_FILE = FileUtil.file(DB_OPS_PATH, "ql-script.json");

    private static final Map<String, Set<String>> EVENT_SCRIPT = new HashMap<>(8);

    @FXML
    public ComboBox<String> dbNameBox;

    @FXML
    public TextField searchText;

    @FXML
    public VBox parentPane;

    @FXML
    private void initialize() {
        dbNameBox.getItems().addAll(DruidSource.listAllNames());
        dbNameBox.getSelectionModel().selectFirst();

        FileUtil.mkdir(DB_OPS_PATH);
        BeanFactory.registerView(TAB_ID, TAB_NAME, this);
        if (FileUtil.exist(SCRIPT_JSON_FILE)) {
            SCRIPTS.clear();
            String json = FileUtil.readUtf8String(SCRIPT_JSON_FILE);
            JSONObject jsonObject = JSON.parseObject(StrUtil.blankToDefault(json, "{}"), Feature.OrderedField,
                    Feature.AllowComment);
            SCRIPTS.putAll(jsonObject);
        }

        renderScripts(null);
    }

    public void search() {
        renderScripts(searchText.getText());
    }

    public void addScript() {
        showQlScriptEditDialog(null);
    }

    private void renderScripts(String search) {
        parentPane.getChildren().clear();

        Insets bottom = new Insets(10, 0, 10, 0);
        Insets right = new Insets(0, 10, 0, 0);
        Insets top = new Insets(3, 0, 0, 0);

        EventHandler<ActionEvent> editHandler = actionEvent -> {
            Button button = (Button) actionEvent.getSource();
            String uuid = button.getParent().getId();
            showQlScriptEditDialog(SCRIPTS.getObject(uuid, QlScript.class));
        };
        EventHandler<ActionEvent> removeHandler = actionEvent -> {
            Button button = (Button) actionEvent.getSource();
            String uuid = button.getParent().getId();
            SCRIPTS.remove(uuid);
            ThreadUtil.execute(() -> FileUtil.writeUtf8String(JSON.toJSONString(SCRIPTS, true), SCRIPT_JSON_FILE));
            renderScripts(null);
        };
        EventHandler<ActionEvent> actionHandler = actionEvent -> {
            Button button = (Button) actionEvent.getSource();
            String uuid = button.getParent().getId();
            QlScript qlScript = SCRIPTS.getObject(uuid, QlScript.class);
            String dbName = StrUtil.blankToDefault(qlScript.getSpecifyDbName(), dbNameBox.getValue());

            ThreadUtil.execute(() -> {
                try {
                    ScriptExecutor.execute(dbName, qlScript.getCodes(), null);
                } catch (Exception e) {
                    FxDialogs.showException("执行脚本失败", e);
                }
            });
        };

        for (String uuid : SCRIPTS.keySet()) {
            QlScript qlScript = SCRIPTS.getObject(uuid, QlScript.class);
            if (StrUtil.isNotBlank(search)) {
                if (!qlScript.getName().contains(search) && !qlScript.getComment().contains(search)) {
                    continue;
                }
            }

            HBox hBox = new HBox();
            hBox.setId(uuid);

            Button action = new Button(qlScript.getName());
            action.setOnAction(actionHandler);
            Button edit = new Button("编辑");
            edit.setOnAction(editHandler);
            Button remove = new Button("删除");
            remove.setOnAction(removeHandler);
            Label label = new Label();

            String labelText = "触发机制：" + ScriptEditController.TYPE_2_TIP.get(qlScript.getType().name());
            if (qlScript.getType() == ExecuteTypeEnum.EVENT) {
                labelText += "，订阅事件：" + qlScript.getEventKey();
                eventSubscribe(qlScript.getEventKey(), uuid);
            }
            if (StrUtil.isNotBlank(qlScript.getSpecifyDbName())) {
                labelText += "，指定数据源：" + qlScript.getSpecifyDbName();
            }
            if (StrUtil.isNotBlank(qlScript.getComment())) {
                labelText += "，说明：" + qlScript.getComment();
            }
            label.setText(labelText);

            HBox.setHgrow(action, Priority.NEVER);
            HBox.setHgrow(edit, Priority.NEVER);
            HBox.setHgrow(remove, Priority.NEVER);
            HBox.setHgrow(label, Priority.ALWAYS);
            HBox.setMargin(action, right);
            HBox.setMargin(edit, right);
            HBox.setMargin(remove, right);
            HBox.setMargin(label, top);
            hBox.getChildren().addAll(action, edit, remove, label);

            VBox.setVgrow(hBox, Priority.NEVER);
            Separator separator = new Separator();
            VBox.setVgrow(separator, Priority.NEVER);
            VBox.setMargin(separator, bottom);
            parentPane.getChildren().addAll(hBox, separator);
        }
    }

    private void showQlScriptEditDialog(QlScript qlScript) {
        FXMLLoader fxmlLoader = new FXMLLoader();
        fxmlLoader.setClassLoader(WetoolSupporter.class.getClassLoader());
        fxmlLoader.setLocation(MainController.class.getResource("/ease/dbops/ScriptEditView.fxml"));

        Node node;
        try {
            node = fxmlLoader.load();
        } catch (IOException e) {
            log.error(ExceptionUtil.stacktraceToString(e, Integer.MAX_VALUE));
            FxDialogs.showError("加载视图发生异常");
            return;
        }

        ScriptEditController controller = fxmlLoader.getController();
        controller.setQlScript(qlScript);
        FxDialogs.showDialog("编辑QL脚本", node, new DialogWinnable<String>() {

            @Override
            public String convertResult() {
                return "ok";
            }

            @Override
            public void consumeResult(String result) {
                if (StrUtil.isEmpty(result)) {
                    return;
                }
                QlScript newScript = controller.getQlScript();
                SCRIPTS.put(newScript.getUuid(), newScript);
                renderScripts(null);
                ThreadUtil.execute(() -> FileUtil.writeUtf8String(JSON.toJSONString(SCRIPTS, true), SCRIPT_JSON_FILE));
            }
        });
    }

    private void eventSubscribe(String eventKey, String uuid) {
        if (StrUtil.isBlank(eventKey) || StrUtil.isBlank(uuid)) {
            return;
        }
        Set<String> scripts = EVENT_SCRIPT.computeIfAbsent(eventKey, s -> new HashSet<>());
        if (scripts.contains(uuid)) {
            // 已订阅
            return;
        }
        EVENT_SCRIPT.forEach((k, v) -> v.remove(uuid));
        scripts.add(uuid);
        EventCenter.subscribeEvent(eventKey, (key, date, eventMessage) -> {
            Set<String> set = EVENT_SCRIPT.get(eventKey);
            if (CollUtil.isEmpty(set)) {
                return;
            }
            set.forEach(e -> {
                QlScript qlScript = SCRIPTS.getObject(uuid, QlScript.class);
                String dbName = StrUtil.blankToDefault(qlScript.getSpecifyDbName(), dbNameBox.getValue());
                try {
                    ScriptExecutor.execute(dbName, qlScript.getCodes(), Map.of("eventMessage", eventMessage));
                } catch (Exception x) {
                    log.error("execute event script error: {}", ExceptionUtil.stacktraceToString(x, Integer.MAX_VALUE));
                }
            });
        });
    }

    public void searchIfEnter(KeyEvent keyEvent) {
        FxUtils.enterDo(keyEvent, this::search);
    }
}
