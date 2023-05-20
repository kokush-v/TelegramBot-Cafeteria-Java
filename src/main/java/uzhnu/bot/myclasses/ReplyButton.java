package uzhnu.bot.myclasses;

public class ReplyButton {
    private String callBackText;
    private String callBackData;

    public ReplyButton(String callBackText, String callBackData) {
        this.callBackText = callBackText;
        this.callBackData = callBackData;
    }

    public String getCallBackText() {
        return callBackText;
    }

    public String getCallBackData() {
        return callBackData;
    }

}
