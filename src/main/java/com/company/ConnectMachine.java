package com.company;

import com.vk.api.sdk.client.TransportClient;
import com.vk.api.sdk.client.VkApiClient;
import com.vk.api.sdk.client.actors.GroupActor;
import com.vk.api.sdk.client.actors.UserActor;
import com.vk.api.sdk.httpclient.HttpTransportClient;
import com.vk.api.sdk.objects.GroupAuthResponse;
import com.vk.api.sdk.objects.UserAuthResponse;
import com.vk.api.sdk.objects.apps.responses.GetFriendsListResponse;
import com.vk.api.sdk.objects.enums.MessagesRev;
import com.vk.api.sdk.objects.groups.Filter;
import com.vk.api.sdk.objects.groups.responses.CreateResponse;
import com.vk.api.sdk.objects.groups.responses.GetByIdLegacyResponse;
import com.vk.api.sdk.objects.groups.responses.GetResponse;
import com.vk.api.sdk.objects.messages.ConversationPeerType;
import com.vk.api.sdk.objects.messages.ConversationWithMessage;
import com.vk.api.sdk.objects.messages.responses.GetConversationsResponse;
import com.vk.api.sdk.objects.messages.responses.GetHistoryResponse;
import com.vk.api.sdk.objects.users.Fields;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.net.URI;
import java.net.URL;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class ConnectMachine {
    VkApiClient vk = null;
    GroupActor gActor = null;
    UserActor uActor = null;
    SecureRandom rnd = null;

    final private int APP_ID = 0;
    final private boolean GROUP_MODE = false;
    final private String REDIRECT_URI = "https://oauth.vk.com/blank.html";
    final private String SECRET_APP_KEY = "Придумайте сами";


    ConnectMachine() {
        TransportClient transportClient = HttpTransportClient.getInstance();
        vk = new VkApiClient(transportClient);
        rnd = new SecureRandom();
        if (GROUP_MODE) {
            initUserForGroup();
            initGroup();
        } else {
            initUserImplicitFlow();
        }
    }

    @Deprecated
    private void initUserForGroup() {
        if (Main.cache.getToken(true) != null) {
            try {
                uActor = new UserActor(Main.cache.getId(true), Main.cache.getToken(true));
                return;
            } catch (Exception e) {
                JOptionPane.showMessageDialog(null, "Invalid cache");
            }
        }

        String userGroupAccessCodeURI = "https://oauth.vk.com/authorize?";
        userGroupAccessCodeURI += "client_id=" + APP_ID;
        userGroupAccessCodeURI += "&redirect_uri=" + REDIRECT_URI;
        userGroupAccessCodeURI += "&scope=" + ((1 << 1) + (1 << 18) + (1 << 16)); // friends and groups forever

        try {
            Desktop.getDesktop().browse(new URI(userGroupAccessCodeURI));
        } catch (Exception e) {
            System.out.println(e);
            System.exit(1);
        }

        String linkWithCode = JOptionPane.showInputDialog("Скопируйте ссылку из адресной строки для получения кода");
        String code = parseCode(linkWithCode);

        try {
            UserAuthResponse userAuthResponse = vk.oAuth()
                    .userAuthorizationCodeFlow(APP_ID, SECRET_APP_KEY, REDIRECT_URI, code)
                    .execute();
            uActor = new UserActor(userAuthResponse.getUserId(), userAuthResponse.getAccessToken());
            Main.cache.setToken(uActor.getId(), uActor.getAccessToken(), true);
        } catch (Exception e) {
            System.out.println(e);
            System.exit(1);
        }
    }

    @Deprecated
    private void initGroup() {
        if (Main.cache.getToken(false) != null) {
            try {
                gActor = new GroupActor(Main.cache.getId(false), Main.cache.getToken(false));
                return;
            } catch (Exception e) {
                JOptionPane.showMessageDialog(null, "Invalid cache");
            }
        }

        GetResponse groupList = null;
        try {
            groupList = vk.groups()
                    .get(uActor).
                    filter(Filter.ADMIN)
                    .execute();
        } catch (Exception e) {
            System.out.println(e);
            System.out.println("Exception while getting user's groups");
            System.exit(1);
        }

        ArrayList<String> goodGroups = new ArrayList<>();
        for (Integer groupId : groupList.getItems()) {
            goodGroups.add(groupId.toString());
        }

        Integer appGroupId = getAppGroup(goodGroups);

        if (appGroupId == null) {
            try {
                CreateResponse newGroupData = vk.groups()
                        .create(uActor, getValidGroupName(uActor.getId()))
                        .execute();
                appGroupId = newGroupData.getId();
                vk.groups().edit(uActor, appGroupId).messages(true).execute();
            } catch (Exception e) {
                System.out.println(e);
                System.out.println("Failed to create a new group");
                System.exit(1);
            }
        }

        String groupAccessCodeURI = "https://oauth.vk.com/authorize?";
        groupAccessCodeURI += "client_id=" + APP_ID;
        groupAccessCodeURI += "&redirect_uri=" + REDIRECT_URI;
        groupAccessCodeURI += "&group_ids=" + appGroupId;
        groupAccessCodeURI += "&scope=" + ((1 << 12)); // messages forever

        try {
            Desktop.getDesktop().browse(new URI(groupAccessCodeURI));
        } catch (Exception e) {
            System.out.println(e);
            System.exit(1);
        }

        String linkWithGroupCode = JOptionPane.showInputDialog("Для доступа к сообщениям специальной группы вставьте ссылку");
        String groupCode = parseCode(linkWithGroupCode);

        try {
            GroupAuthResponse groupAuthResponse = vk.oAuth()
                    .groupAuthorizationCodeFlow(APP_ID, SECRET_APP_KEY, REDIRECT_URI, groupCode)
                    .execute();
            gActor = new GroupActor(appGroupId, groupAuthResponse.getAccessTokens().get(appGroupId));
            Main.cache.setToken(appGroupId, gActor.getAccessToken(), false);
        } catch (Exception e) {
            System.out.println(e);
            System.out.println("Error while getting group access");
            System.exit(1);
        }
    }

    private void initUserImplicitFlow() {
        if (Main.cache.getToken(true) != null) {
            try {
                uActor = new UserActor(Main.cache.getId(true), Main.cache.getToken(true));
                return;
            } catch (Exception e) {
                JOptionPane.showMessageDialog(null, "Invalid cache");
            }
        }

        String implicitFlowURI = "https://oauth.vk.com/authorize?";
        implicitFlowURI += "client_id=" + APP_ID;
        implicitFlowURI += "&redirect_uri=" + REDIRECT_URI;
        implicitFlowURI += "&display=page";
        implicitFlowURI += "&response_type=token";
        implicitFlowURI += "&scope=" + ((1 << 12) + (1 << 16)); // messages forever

        try {
            Desktop.getDesktop().browse(new URI(implicitFlowURI));
        } catch (Exception e) {
            System.out.println(e);
            System.exit(1);
        }

        String link = JOptionPane.showInputDialog("Скопируйте адрес из браузера");

        Integer id = parseImplicitFlowUserId(link);
        String token = parseImplicitFlowUserToken(link);
        System.out.println(token);

        uActor = new UserActor(id, token);
        Main.cache.setToken(uActor.getId(), uActor.getAccessToken(), true);
    }

    private Integer getAppGroup(ArrayList<String> possibleGroups) {
        for (String s : possibleGroups) {
            System.out.println(s);
        }
        String validName = getValidGroupName(uActor.getId());
        try {
            List<GetByIdLegacyResponse> getGroups = vk.groups()
                    .getByIdLegacy(uActor)
                    .groupIds(possibleGroups)
                    .execute();
            for (GetByIdLegacyResponse group : getGroups) {
                if (group.getName().equals(validName)) {
                    return group.getId();
                }
            }
        } catch (Exception e) {
            System.out.println(e);
            System.out.println("An exception while getting info about groups where admin");
        }
        return null;
    }

    private String getValidGroupName(Integer userId) {
        return userId.toString() + "VKEapp";
    }

    private String parseCode(String link) {
        int ind = link.length() - 1;
        while (link.charAt(ind) != '=') {
            ind--;
        }
        return link.substring(ind + 1);
    }

    private Integer parseImplicitFlowUserId(String link) {
        int pos = link.indexOf("user_id=");
        pos += 8; // first id char
        StringBuilder id = new StringBuilder();
        while (pos < link.length() && link.charAt(pos) != '&') {
            id.append(link.charAt(pos));
            pos++;
        }
        return Integer.parseInt(id.toString());
    }

    private String parseImplicitFlowUserToken(String link) {
        int pos = link.indexOf("access_token=");
        pos += 13; // first id char
        StringBuilder token = new StringBuilder();
        while (pos < link.length() && link.charAt(pos) != '&') {
            token.append(link.charAt(pos));
            pos++;
        }
        return token.toString();
    }

    public Dialogue[] getDialogues() {
        ArrayList<ConversationWithMessage> convs = new ArrayList<>();
        List<com.vk.api.sdk.objects.users.responses.GetResponse> usersData = new ArrayList<>();
        int offset = 0, allConversations;
        try {
            do {
                GetConversationsResponse dialogues = vk.messages()
                        .getConversations(uActor)
                        .offset(offset)
                        .count(200)
                        .execute();

                ArrayList<String> dialogueIds = new ArrayList<>();
                //System.out.println(dialogues.getItems().size());
                for (ConversationWithMessage d : dialogues.getItems()) {
                    if (d.getConversation().getPeer().getType() == ConversationPeerType.USER) {
                        convs.add(d);
                        dialogueIds.add(d.getConversation().getPeer().getId().toString());
                    }
                    offset++;
                }
                allConversations = dialogues.getCount();

                List<com.vk.api.sdk.objects.users.responses.GetResponse> usersInfo = vk.users()
                        .get(uActor)
                        .userIds(dialogueIds)
                        .fields(Fields.FIRST_NAME_ABL, Fields.LAST_NAME_ABL, Fields.PHOTO_50)
                        .execute();
                for (com.vk.api.sdk.objects.users.responses.GetResponse resp : usersInfo) {
                    usersData.add(resp);
                }
            } while (offset < allConversations);
        } catch (Exception e) {
            System.out.println(e);
            System.out.println("An error while getting dialogues");
            System.exit(1);
        }

        System.out.println(convs.size() + " " + usersData.size());

        Dialogue[] dialoguesList = new Dialogue[convs.size()];
        int i = 0;
        for (ConversationWithMessage currentDialogue : convs) {
            Integer peer = currentDialogue.getConversation().getPeer().getId();

            String name = usersData.get(i).getFirstName() + " " + usersData.get(i).getLastName();

            dialoguesList[i] = new Dialogue(peer, this, name, getDialogueImagePath(usersData.get(i).getPhoto50(), peer));
            i++;
        }
        return dialoguesList;
    }

    public Message[] getAllMessagesFromDialogue(int peer) {
        ArrayList<Message> dialogueHistory = new ArrayList<>();
        try {
            GetHistoryResponse response = vk.messages()
                        .getHistory(uActor)
                        .count(200)
                        .peerId(peer)
                        .rev(MessagesRev.REVERSE_CHRONOLOGICAL).extended(true).execute();

            for (com.vk.api.sdk.objects.messages.Message msg : response.getItems()) {
                dialogueHistory.add(new Message(msg.getText(), msg.getFromId().equals(uActor.getId())));
            }
            Collections.reverse(dialogueHistory);
        } catch (Exception e) {
            System.out.println(e);
            System.out.println("An error while getting dialogue history");
        }
        return dialogueHistory.toArray(new Message[0]);
    }

    public String getDialogueImagePath(URI webLink, int peer) {
        File file = new File(Main.pathPrefix + "images/dial" + peer + ".png");
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (Exception e) {
                System.out.println("Fucked while creating image file");
            }
        } else {
            return "images/dial" + peer + ".png";
        }
        BufferedImage img = null;
        try {
            img = ImageIO.read(webLink.toURL());
        } catch (Exception e) {
            System.out.println("An error while getting image from site " + webLink);
        }
        try {
            ImageIO.write(img, "png", file);
        } catch (Exception e) {
            System.out.println("Fucked while writing image in file");
        }
        return "images/dial" + peer + ".png";
    }

    public void sendTextMessage(int dialogueId, String msg) {
        try {
            vk.messages()
                    .send(uActor)
                    .peerId(dialogueId)
                    .message(msg)
                    .randomId(rnd.nextInt())
                    .execute();
        } catch (Exception e) {
            System.out.println("Failed to send a message");
        }
    }
}
