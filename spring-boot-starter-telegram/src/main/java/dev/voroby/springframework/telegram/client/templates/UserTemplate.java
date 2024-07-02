package dev.voroby.springframework.telegram.client.templates;

import dev.voroby.springframework.telegram.client.TdApi;
import dev.voroby.springframework.telegram.client.TelegramClient;
import dev.voroby.springframework.telegram.client.templates.response.Response;
import org.springframework.util.CollectionUtils;

import java.util.Collections;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

/**
 * This class simplifies the use of {@link TelegramClient} for {@link TdApi.User} related objects.
 *
 * @author Pavel Vorobyev
 */
public class UserTemplate {

    private final TelegramClient telegramClient;

    public UserTemplate(TelegramClient telegramClient) {
        this.telegramClient = telegramClient;
    }

    /**
     * Returns information about a user by their identifier. This is an offline request.
     *
     * @param userId User identifier.
     * @return {@link CompletableFuture<Response<TdApi.User>>}.
     */
    public CompletableFuture<Response<TdApi.User>> getUser(long userId) {
        return telegramClient.sendAsync(new TdApi.GetUser(userId));
    }

    /**
     * Returns full information about a user by their identifier.
     *
     * @param userId User identifier.
     * @return {@link CompletableFuture<Response<TdApi.UserFullInfo>>}.
     */
    public CompletableFuture<Response<TdApi.UserFullInfo>> getUserFullInfo(long userId) {
        return telegramClient.sendAsync(new TdApi.GetUserFullInfo(userId));
    }

    /**
     * Returns an HTTPS link, which can be used to get information about the current user.
     *
     * @return {@link CompletableFuture<Response<TdApi.UserLink>>}.
     */
    public CompletableFuture<Response<TdApi.UserLink>> getUserLink() {
        return telegramClient.sendAsync(new TdApi.GetUserLink());
    }

    /**
     * Returns the current user.
     *
     * @return {@link CompletableFuture<Response<TdApi.User>>}.
     */
    public CompletableFuture<Response<TdApi.User>> getMe() {
        return telegramClient.sendAsync(new TdApi.GetMe());
    }


    /**
     * Returns profile photo of the user. May be null.
     *
     * @param userId User identifier.
     * @return {@link CompletableFuture<Response<TdApi.ProfilePhoto>>}. TdApi.ProfilePhoto may be null.
     */
    public CompletableFuture<Response<TdApi.ProfilePhoto>> getProfilePhoto(long userId) {
        return getUser(userId).thenApply(userResponse -> {
            if (userResponse.error() != null) {
                return new Response<>(null, userResponse.error());
            }
            return new Response<>(userResponse.object().profilePhoto, null);
        });
    }


    /**
     * Returns user profile photo visible if the main photo is hidden by privacy settings. May be null.
     *
     * @param userId User identifier.
     * @return {@link CompletableFuture<Response<TdApi.ChatPhoto>>}. TdApi.ChatPhoto may be null.
     */
    public CompletableFuture<Response<TdApi.ChatPhoto>> getPublicPhoto(long userId) {
        return getUserFullInfo(userId)
                .thenApply(userFullInfoResponse -> {
                    if (userFullInfoResponse.error() != null) {
                        return new Response<>(null, userFullInfoResponse.error());
                    }
                    return new Response<>(userFullInfoResponse.object().publicPhoto, null);
                });
    }

    /**
     * Returns the profile photos of a user. Personal and public photo aren't returned.
     *
     * @param userId User identifier.
     * @param offset The number of photos to skip; must be non-negative.
     * @param limit The maximum number of photos to be returned; up to 100.
     * @return {@link CompletableFuture<Response<TdApi.ChatPhotos>>}.
     */
    public CompletableFuture<Response<TdApi.ChatPhotos>> getUserProfilePhotos(long userId, int offset, int limit) {
        return telegramClient.sendAsync(new TdApi.GetUserProfilePhotos(userId, offset, limit));
    }

    /**
     * Searches a user by their phone number. Returns null if user can't be found.
     *
     * @param phoneNumber Phone number in international format to search for.
     * @return {@link CompletableFuture<Response<TdApi.User>>}. TdApi.User may be null.
     */
    public CompletableFuture<Response<TdApi.User>> searchUserByPhoneNumber(String phoneNumber) {
        Objects.requireNonNull(phoneNumber);
        return telegramClient.sendAsync(new TdApi.SearchUserByPhoneNumber(phoneNumber, false));
    }
    public CompletableFuture<Response<TdApi.Chats>> searchChats(String query) {
        Objects.requireNonNull(query);
        TdApi.SearchChats searchChats = new TdApi.SearchChats(query, 2);
        //TdApi.SearchPublicChats searchPublicChats = (TdApi.SearchPublicChats) searchChats;
        return telegramClient.sendAsync(searchChats);
    }
    public CompletableFuture<Response<TdApi.FoundChatMessages>> searchChatMessages(String chatName, String query) {
        Objects.requireNonNull(query);
        Response<TdApi.Chats> chatsResponse = searchChats(chatName).join();
        long[] chatIds = chatsResponse.object().chatIds;
        if (CollectionUtils.isEmpty(Collections.singleton(chatIds))) {
            return null;
        }
        TdApi.SearchChatMessages searchChatMessages = new TdApi.SearchChatMessages(
                chatIds[0], query, null, 0, 0, 1, null, 0, 0);
        return telegramClient.sendAsync(searchChatMessages);
    }
    /**
     * Searches a user by username. Returns null if user can't be found.
     *
     * @param username Username to search for.
     * @return {@link CompletableFuture<Response<TdApi.User>>}. TdApi.User may be null.
     */
    public CompletableFuture<Response<TdApi.User>> searchUserByUsername(String username) {
        Objects.requireNonNull(username);
        return telegramClient.sendAsync(new TdApi.SearchPublicChat(username))
                .thenCompose(chatResponse -> {
                    if (chatResponse.error() != null) {
                        return CompletableFuture.completedFuture(new Response<>(null, chatResponse.error()));
                    }
                    if (chatResponse.object().type instanceof TdApi.ChatTypePrivate typePrivate) {
                        return getUser(typePrivate.userId);
                    }
                    return CompletableFuture.completedFuture(new Response<>(null, null));
                });
    }

}
