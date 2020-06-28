package com.botmasterzzz.social.dto;

import com.fasterxml.jackson.annotation.*;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonPropertyOrder({
        "id",
        "username",
        "email",
        "name",
        "nickname"
})
public class User extends AbstractDto {

    @JsonProperty("username")
    private String username;
    //    @JsonProperty("email")
    @JsonIgnore
    private String email;
    @JsonProperty("name")
    private String name;
    @JsonProperty("nickname")
    private String nickname;

    public User() {
    }

    public User(String username, String email) {
        this.username = username;
        this.email = email;
    }

    public static User.Builder newBuilder() {
        return new User().new Builder();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    @Override
    public String toString() {
        return "User{" +
                "username='" + username + '\'' +
                ", email='" + email + '\'' +
                ", name='" + name + '\'' +
                ", nickname='" + nickname + '\'' +
                ", id=" + id +
                '}';
    }

    public class Builder {

        private Builder() {

        }

        public User.Builder setId(Long id) {
            User.super.id = id;
            return this;
        }

        public User.Builder setUsername(String username) {
            User.this.username = username;
            return this;
        }


        public User.Builder setEmail(String email) {
            User.this.email = email;
            return this;
        }

        public User.Builder setName(String name) {
            User.this.name = name;
            return this;
        }

        public User.Builder setNickname(String nickname) {
            User.this.nickname = nickname;
            return this;
        }

        public User build() {
            return User.this;
        }

    }
}
