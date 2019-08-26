package org.code4everything.wetool.plugin.ftp;

import com.alibaba.fastjson.JSONObject;
import org.code4everything.wetool.plugin.ftp.config.FtpConfig;
import org.code4everything.wetool.plugin.ftp.config.FtpInfo;
import org.code4everything.wetool.plugin.support.config.WeConfig;
import org.code4everything.wetool.plugin.test.WetoolTest;

/**
 * @author pantao
 * @since 2019/8/24
 */
public class FtpTest {

    public static void main(String[] args) {
        // 模拟配置文件
        WeConfig config = WetoolTest.getConfig();
        JSONObject json = new JSONObject();
        FtpConfig ftpConfig = new FtpConfig();
        ftpConfig.setShowOnStartup(true);

        FtpInfo ftpInfo = new FtpInfo();
        ftpInfo.setName("test");
        ftpInfo.setCharset("utf-8");
        ftpInfo.setAnonymous(false);
        ftpInfo.setHost("192.168.1.234");
        ftpInfo.setPort(21);
        ftpInfo.setUsername("root");
        ftpInfo.setPassword("root");
        ftpInfo.setReconnect(false);
        ftpInfo.setSelect(true);

        ftpConfig.addFtp(ftpInfo);
        json.put(FtpConfig.KEY_CAMEL, ftpConfig);
        config.setConfigJson(json);

        WetoolTest.runTest(new WetoolSupporter(), config, args);
    }
}