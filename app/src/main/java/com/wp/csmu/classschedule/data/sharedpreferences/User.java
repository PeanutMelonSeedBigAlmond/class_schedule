package com.wp.csmu.classschedule.data.sharedpreferences;

import net.nashlegend.anypref.annotations.PrefField;
import net.nashlegend.anypref.annotations.PrefModel;

@PrefModel("user")
public class User {
    @PrefField(value = "account", strDef = "")
    public String account;
    @PrefField(value = "password", strDef = "")
    public String password;

    public User(String account, String password) {
        this.account = account;
        this.password = password;
    }

    public User() {
    }

    public String getAccount() {
        return account;
    }

    public void setAccount(String account) {
        this.account = account;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

}
