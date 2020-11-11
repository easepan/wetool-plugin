package org.code4everything.wetool.plugin.dbops.controller;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import org.code4everything.wetool.plugin.dbops.script.ExecuteTypeEnum;
import org.code4everything.wetool.plugin.dbops.script.SqlScript;
import org.code4everything.wetool.plugin.support.druid.DruidSource;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @author pantao
 * @since 2020/11/11
 */
public class ScriptEditController {

    private static final Map<String, String> TYPE_2_TIP = Map.of("HANDY", "手动触发", "EVENT", "事件触发");

    private static final Map<String, String> TIP_2_TYPE = Map.of("手动触发", "HANDY", "事件触发", "EVENT");

    @FXML
    public TextField nameText;

    @FXML
    public TextField commentText;

    @FXML
    public ComboBox<String> typeBox;

    @FXML
    public ComboBox<String> dbNameBox;

    @FXML
    public TextField eventKeyText;

    @FXML
    public TextArea sqlScriptText;

    private SqlScript sqlScript;

    @FXML
    private void initialize() {
        typeBox.getItems().addAll(TIP_2_TYPE.keySet());
        typeBox.getSelectionModel().selectFirst();

        dbNameBox.getItems().add("");
        dbNameBox.getItems().addAll(DruidSource.listAllNames());
        dbNameBox.getSelectionModel().selectFirst();

        if (Objects.isNull(sqlScript)) {
            return;
        }

        typeBox.getSelectionModel().select(TYPE_2_TIP.get(sqlScript.getType().name()));
        dbNameBox.getSelectionModel().select(sqlScript.getSpecifyDbName());

        nameText.setText(sqlScript.getName());
        commentText.setText(sqlScript.getComment());
        eventKeyText.setText(sqlScript.getEventKey());

        List<List<String>> codeBlocks = sqlScript.getCodeBlocks();
        if (CollUtil.isEmpty(codeBlocks)) {
            return;
        }

        codeBlocks.forEach(block -> {
            block.forEach(line -> {
                sqlScriptText.appendText(line);
                sqlScriptText.appendText(System.lineSeparator());
            });
            sqlScriptText.appendText(System.lineSeparator());
        });
    }

    public SqlScript getSqlScript() {
        if (Objects.isNull(sqlScript)) {
            sqlScript = new SqlScript().setUuid(IdUtil.simpleUUID());
        }

        sqlScript.setName(nameText.getText());
        sqlScript.setComment(commentText.getText());
        sqlScript.setType(ExecuteTypeEnum.valueOf(typeBox.getValue()));
        sqlScript.setEventKey(eventKeyText.getText());
        sqlScript.setSpecifyDbName(dbNameBox.getValue());
        sqlScript.setCodeBlocks(new ArrayList<>());

        String[] lines = StrUtil.split(sqlScriptText.getText(), System.lineSeparator());
        if (ArrayUtil.isEmpty(lines)) {
            return sqlScript;
        }

        List<String> blockCodes = new ArrayList<>();
        for (String line : lines) {
            if (StrUtil.isBlank(line)) {
                blockCodes = new ArrayList<>();
                sqlScript.getCodeBlocks().add(blockCodes);
            } else {
                blockCodes.add(line);
            }
        }

        sqlScript.setCodeBlocks(sqlScript.getCodeBlocks().stream().filter(CollUtil::isNotEmpty).collect(Collectors.toList()));
        return sqlScript;
    }
}
