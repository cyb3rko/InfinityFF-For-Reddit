package ml.docilealligator.infinityforreddit.subreddit;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import ml.docilealligator.infinityforreddit.SortType;
import ml.docilealligator.infinityforreddit.apis.GqlAPI;
import ml.docilealligator.infinityforreddit.apis.GqlRequestBody;
import ml.docilealligator.infinityforreddit.apis.RedditAPI;
import ml.docilealligator.infinityforreddit.utils.APIUtils;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class FetchSubredditData {
    public static void fetchSubredditData(Retrofit oauthRetrofit, Retrofit retrofit, String subredditName, String accessToken, final FetchSubredditDataListener fetchSubredditDataListener) {

        Call<String> subredditData;
        if (accessToken == null) {
            RedditAPI api = retrofit.create(RedditAPI.class);
            subredditData = api.getSubredditData(subredditName);
        } else {
            GqlAPI oauthApi = oauthRetrofit.create(GqlAPI.class);
            Map<String, String> header = APIUtils.getOAuthHeader(accessToken);
            subredditData = oauthApi.getSubredditData(header, GqlRequestBody.subredditDataBody(subredditName));
        }
        subredditData.enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
                if (response.isSuccessful()) {
                    ParseSubredditData.parseSubredditData(response.body(), new ParseSubredditData.ParseSubredditDataListener() {
                        @Override
                        public void onParseSubredditDataSuccess(SubredditData subredditData, int nCurrentOnlineSubscribers) {
                            fetchSubredditDataListener.onFetchSubredditDataSuccess(subredditData, nCurrentOnlineSubscribers);
                        }

                        @Override
                        public void onParseSubredditDataFail() {
                            fetchSubredditDataListener.onFetchSubredditDataFail(false);
                        }
                    });
                } else {
                    fetchSubredditDataListener.onFetchSubredditDataFail(response.code() == 403);
                }
            }

            @Override
            public void onFailure(@NonNull Call<String> call, @NonNull Throwable t) {
                fetchSubredditDataListener.onFetchSubredditDataFail(false);
            }
        });
    }

    static void fetchSubredditListingData(Retrofit retrofit, String query, String after, SortType.Type sortType, String accessToken,
                                          boolean nsfw, final FetchSubredditListingDataListener fetchSubredditListingDataListener) {
        GqlAPI api = retrofit.create(GqlAPI.class);

        Map<String, String> map = new HashMap<>();
        Map<String, String> headers = accessToken != null ? APIUtils.getOAuthHeader(accessToken) : Collections.unmodifiableMap(map);
        RequestBody body = GqlRequestBody.subredditSearchBody(query, nsfw, after);

        Call<String> subredditDataCall = api.searchSubreddit(headers, body);
        subredditDataCall.enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
                if (response.isSuccessful()) {
                    ParseSubredditData.parseSubredditListingData(response.body(), nsfw,
                            new ParseSubredditData.ParseSubredditListingDataListener() {
                                @Override
                                public void onParseSubredditListingDataSuccess(ArrayList<SubredditData> subredditData, String after) {
                                    fetchSubredditListingDataListener.onFetchSubredditListingDataSuccess(subredditData, after);
                                }

                                @Override
                                public void onParseSubredditListingDataFail() {
                                    fetchSubredditListingDataListener.onFetchSubredditListingDataFail();
                                }
                            });
                } else {
                    fetchSubredditListingDataListener.onFetchSubredditListingDataFail();
                }
            }

            @Override
            public void onFailure(@NonNull Call<String> call, @NonNull Throwable t) {
                fetchSubredditListingDataListener.onFetchSubredditListingDataFail();
            }
        });
    }

    public interface FetchSubredditDataListener {
        void onFetchSubredditDataSuccess(SubredditData subredditData, int nCurrentOnlineSubscribers);

        void onFetchSubredditDataFail(boolean isQuarantined);
    }

    interface FetchSubredditListingDataListener {
        void onFetchSubredditListingDataSuccess(ArrayList<SubredditData> subredditData, String after);

        void onFetchSubredditListingDataFail();
    }
}
