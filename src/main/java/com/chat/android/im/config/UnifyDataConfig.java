package com.chat.android.im.config;

/**
 * Created by Ryan on 2020/9/30.
 */
public class UnifyDataConfig {

    private String base;
    private String rid;
    private String id;
    private String username;
    private String password;
    private String token;
    private String welcome;
    private int preLoadHistoryCount;

    private UnifyDataConfig(Builder builder) {
        this.base = builder.base;
        this.rid = builder.rid;
        this.id = builder.id;
        this.token = builder.token;
        this.username = builder.username;
        this.password = builder.password;
        this.welcome = builder.welcome;
        this.preLoadHistoryCount = builder.preLoadHistoryCount;
    }

    public String getBase() {
        return base;
    }

    public String getRid() {
        return rid;
    }

    public String getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public String getToken() {
        return token;
    }

    public String getWelcome() {
        return welcome;
    }

    public int getPreLoadHistoryCount() {
        return preLoadHistoryCount;
    }

    public static class Builder {
        private String base;
        private String rid;
        private String id;
        private String token;
        private String username;
        private String password;
        private String welcome;
        private int preLoadHistoryCount = 10;


        /**
         * Point your client to the Websocket of the server you want to connect to
         * wss://[ABC.DOMAIN.COM]/websocket
         */
        public Builder setUrl(String url) {
            this.base = url;
            return this;
        }

        public Builder setRoomId(String rid) {
            this.rid = rid;
            return this;
        }

        public Builder setUserId(String id) {
            this.id = id;
            return this;
        }

        public Builder setUserToken(String token) {
            this.token = token;
            return this;
        }

        public Builder setWelcome(String welcome) {
            this.welcome = welcome;
            return this;
        }

        public Builder setPreLoadHistoryCount(int preLoadHistoryCount) {
            this.preLoadHistoryCount = preLoadHistoryCount;
            return this;
        }

        public UnifyDataConfig build() {
            return new UnifyDataConfig(this);
        }

    }
}
