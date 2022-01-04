package com.hsn.epic4j.start;

import cn.hutool.core.util.StrUtil;
import com.hsn.epic4j.config.EpicConfig;
import com.hsn.epic4j.exception.CheckException;
import com.hsn.epic4j.exception.PermissionException;
import com.hsn.epic4j.exception.TimeException;
import com.hsn.epic4j.util.PageUtil;
import com.ruiyun.jvppeteer.core.page.Page;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class PasswordLogin implements ILogin {
    @Autowired
    EpicConfig epicConfig;

    @Override
    @SneakyThrows
    public void login(Page page) {

        if (StrUtil.isEmpty(epicConfig.getEmail())) {
            throw new CheckException("账号不能为空");
        }
        if (StrUtil.isEmpty(epicConfig.getPassword())) {
            throw new CheckException("密码不能为空");
        }
        log.debug("login start");

        PageUtil.click(page, "div.menu-icon");
        PageUtil.click(page, "div.mobile-buttons a[href='/login']");
        PageUtil.click(page, "#login-with-epic");
        PageUtil.type(page, "#email", epicConfig.getEmail());
        PageUtil.type(page, "#password", epicConfig.getPassword());
        PageUtil.click(page, "#sign-in[tabindex='0']");
//        page.waitForSelector("#rememberMe").click();
        Integer result = PageUtil.findSelectors(page, 30000, true, "#talon_frame_login_prod[style*=visible]", "div.MuiPaper-root[role=alert] h6[class*=subtitle1]", "input[name=code-input-0]", "#user");

        switch (result) {
            case -1:
                throw new TimeException("Check login result timeout.");
            case 0:
                throw new PermissionException("CAPTCHA is required for unknown reasons when logging in");
            case 1: {
                Object jsonValue = page.waitForSelector("div.MuiPaper-root[role=alert] h6[class*=subtitle1]")
                        .getProperty("textContent").jsonValue();
                throw new PermissionException("From Epic Games: " + jsonValue);
            }
            case 2: {
                throw new PermissionException("From Epic Games need code");
            }
        }
        log.debug("login end");
    }
}