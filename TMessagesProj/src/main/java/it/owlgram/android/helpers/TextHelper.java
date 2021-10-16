package it.owlgram.android.helpers;

import org.telegram.tgnet.TLRPC;

public class TextHelper {

    public int start;
    public int end;
    public String flag;

    public TextHelper(){}
    public TextHelper(TextHelper run) {
        start = run.start;
        end = run.end;
        flag = run.flag;
    }
    public void merge(TextHelper run) {
        flag += "-" + run.flag;
    }
    public void setFlag(TLRPC.MessageEntity entity) {
        if(entity instanceof TLRPC.TL_messageEntityBold){
            flag = "b";
        }else if(entity instanceof TLRPC.TL_messageEntityItalic){
            flag = "i";
        }else if(entity instanceof TLRPC.TL_messageEntityUnderline){
            flag = "u";
        }else if(entity instanceof TLRPC.TL_messageEntityStrike){
            flag = "s";
        }else if(entity instanceof TLRPC.TL_messageEntityCode){
            flag = "p";
        } else if(entity instanceof TLRPC.TL_messageEntityPre){
            flag = "c";
        } else if (entity instanceof TLRPC.TL_messageEntityBlockquote) {
            flag = "q";
        }else{
            flag = "a";
        }
    }
}
