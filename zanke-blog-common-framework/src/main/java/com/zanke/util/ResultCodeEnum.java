package com.zanke.util;

import lombok.Getter;

/**
 * 统一返回结果状态信息类
 */
@Getter
public enum ResultCodeEnum {

    SUCCESS(200,"success"),
    SYSTEM_ERROR(400,"system error"),
    NEED_LOGIN(401,"need login"),
    USERNAME_OR_PASSWORD_ERROR(501,"username or password error"),
    FILE_TYPE_ERROR(502,"file type error"),
    NOTLOGIN(504,"not Login"),
    USERNAME_USED(505,"username is occupied"),
    HAVE_NO_AUTHORITY(506,"have no authority"),
    COMMENT_EMPTY_ERROR(507,"comment is empty"),
    USERNAME_EMPTY_ERROR(508,"username is empty"),
    PASSWORD_EMPTY_ERROR(509,"password is empty"),
    USER_NOT_EXIST(510,"user not exist"),
    EMAIL_EMPTY_ERROR(511,"email is empty"),
    NICKNAME_EMPTY_ERROR(512,"nickname is empty"),
    NICK_NAME_USED(513,"nickname is occupied"),
    USER_NOT_ADMIN(514,"user is not admin"),
    TAG_NAME_EMPTY_ERROR(515,"tag name is empty"),
    TAG_REMARK_EMPTY_ERROR(516,"tag remark is empty"),
    TAG_ADD_ERROR(517,"tag add error"),
    TAG_DELETE_ERROR(518,"tag delete error"),
    COMMENT_ADD_ERROR(519,"comment add error"),
    TAG_UPDATE_ERROR(520,"tag update error"),
    IMAGE_UPLOAD_ERROR(521,"image upload error"),
    ADD_ARTICLE_ERROR(522,"add article error"),
    ADD_ARTICLE_TAG_ERROR(523,"add article tag error"),
    ARTICLE_TITLE_EMPTY_ERROR(524,"article title is empty"),
    ARTICLE_SUMMARY_EMPTY_ERROR(525,"article summary is empty"),
    ARTICLE_CONTENT_EMPTY_ERROR(526,"article content is empty"),
    ARTICLE_TAG_EMPTY_ERROR(527,"article tag is empty"),
    ARTICLE_CATEGORY_EMPTY_ERROR(528,"article category is empty"),
    MENU_PARENT_ERROR(529,"the parent can not be itself"),
    HAVE_CHILDREN(530,"menu has children,can not be deleted"),
    CAN_NOT_DELETE(531,"can not delete"),
    ROLE_NAME_EMPTY_ERROR(532,"role name is empty"),
    ROLE_KEY_EMPTY_ERROR(533,"role key is empty"),
    CATEGORY_NAME_EMPTY_ERROR(534,"category name is empty"),
    CATEGORY_DESCRIPTION_EMPTY_ERROR(535,"category description is empty"),
    ROLE_REMARK_EMPTY_ERROR(536,"role remark is empty"),
    CATEGORY_HAS_ARTICLE_ERROR(537, "There are articles under this category"),
    LINK_NAME_EMPTY_ERROR(538, "link name is empty"),
    ADDRESS_EMPTY_ERROR(539, "address is empty"),
    DESCRIPTION_EMPTY_ERROR(540, "description is empty"),
    LOGO_EMPTY_ERROR(541, "logo is empty"),
    MENU_TYPE_ERROR(542, "menu type error"),
    MENU_NAME_EMPTY(543, "menu name is empty"),
    MENU_ORDER_NUM_EMPTY(544, "menu order num is empty"),
    MENU_PATH_EMPTY(545, "menu path is empty"),
    MENU_ICON_EMPTY(546, "menu icon is empty"),
    MENU_PARENT_EMPTY(547, "menu parent is empty"),
    MENU_COMPONENT_EMPTY(548, "menu component is empty"),
    MENU_PERMS_EMPTY(549, "menu perms is empty"),
    CATEGORY_ILLEGAL_ERROR(550, "category is illegal"),
    MENU_ENABLE_ERROR(551, "there are roles under this menu"),
    ROLE_ENABLE_ERROR(552, "there are users under this role"),
    MENU_ILLEGAL_ERROR(553, "menus illegal"),
    ROLE_ILLEGAL_ERROR(554, "roles illegal"),
    CAN_NOT_FORBIDDEN(555, "can not forbidden"),
    TAG_HAS_ARTICLE_ERROR(556, "there are  articles under this tag"),
    TAG_ILLEGAL(557, "tags illegal"),
    FILE_ERROR(558, "file error"),
    FILE_SIZE_ERROR(559, "file size error");

    private Integer code;
    private String message;
    ResultCodeEnum(Integer code, String message) {
        this.code = code;
        this.message = message;
    }
}