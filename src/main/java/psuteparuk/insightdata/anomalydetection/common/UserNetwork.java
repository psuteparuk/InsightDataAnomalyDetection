package psuteparuk.insightdata.anomalydetection.common;

public class UserNetwork extends SocialNetwork<UserData> {
    @Override
    public UserData initializeData(String userId) {
        return new UserData(userId, 2);
    }
}
