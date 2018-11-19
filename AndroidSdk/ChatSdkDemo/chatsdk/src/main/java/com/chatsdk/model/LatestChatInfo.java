package com.chatsdk.model;

import com.alibaba.fastjson.annotation.JSONField;
import com.chatsdk.controller.ChatServiceController;
import com.chatsdk.controller.JniController;
import com.chatsdk.util.FilterWordsManager;

import org.apache.commons.lang.StringUtils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LatestChatInfo
{
	private String	name;
	private String	asn;
	private String	msg;
	private int		vip;
	private int		svip;
	private int		isVersionValid;
	private int		sequenceId;
	private int		createTime;
	private int		post;
	private int 	colorIndex;
	private String  attachment;
	private String  dialog;
	private String  uid;
	/** 系统头像 */
	public String			headPic;
	/** 自定义头像 */
	public int				headPicVer;

	/** 多媒体 */
	public String 	media;

	public String getName()
	{
		return name;
	}

	public void setName(String name)
	{
		this.name = name;
	}

	public String getAsn()
	{
		return asn;
	}

	public void setAsn(String asn)
	{
		this.asn = asn;
	}

	public String getMsg()
	{
		return msg;
	}

	public void setMsg(String msg)
	{
		this.msg = msg;
	}

	public int getVip()
	{
		return vip;
	}

	public void setVip(int vip)
	{
		this.vip = vip;
	}

	public int getSvip()
	{
		return svip;
	}

	public void setSvip(int svip)
	{
		this.svip = svip;
	}

	public int getIsVersionValid()
	{
		return isVersionValid;
	}

	public void setIsVersionValid(int isVersionValid)
	{
		this.isVersionValid = isVersionValid;
	}

	public int getCreateTime()
	{
		return createTime;
	}

	public void setCreateTime(int createTime)
	{
		this.createTime = createTime;
	}

	public int getSequenceId()
	{
		return sequenceId;
	}

	public void setSequenceId(int sequenceId)
	{
		this.sequenceId = sequenceId;
	}

	public int getPost()
	{
		return post;
	}

	public void setPost(int post)
	{
		this.post = post;
	}
	public int getColorIndex()
	{
		return colorIndex;
	}
	public void setColorIndex(int colorIndex)
	{
		this.colorIndex = colorIndex;
	}
	public String getAttachment()
	{
		return attachment;
	}
	public void setAttachment(String attachment)
	{
		this.attachment = attachment;
	}
	public String getDialog()
	{
		return dialog;
	}
	public void setDialog(String dialog)
	{
		this.dialog = dialog;
	}
	
	public String getUid()
	{
		return uid;
	}
	public void setUid(String uid)
	{
		this.uid = uid;
	}
	
	public String getHeadPic()
	{
		return this.headPic;
	}

	public int getHeadPicVer()
	{
		return this.headPicVer;
	}

	@JSONField(serialize = false)
	public void setMsgInfo(MsgItem msgItem)
	{
		this.name = msgItem.getName();
		this.asn = msgItem.getASN();
		this.msg = StringUtils.isNotEmpty(msgItem.translateMsg) ? msgItem.translateMsg : msgItem.msg;
		this.vip = msgItem.getVipLevel();
		this.svip = msgItem.getSVipLevel();
		this.isVersionValid = msgItem.isVersionInvalid() ? 0 : 1;
		this.sequenceId = msgItem.sequenceId;
		this.createTime = msgItem.createTime;
		this.post = msgItem.post;
		this.uid = msgItem.uid;
		this.headPic = msgItem.getHeadPic();
		this.headPicVer = msgItem.getHeadPicVer();
		this.attachment = msgItem.attachmentId;
		this.media = msgItem.media;

		if(this.post==MsgItem.MSG_TYPE_CREATE_EQUIP_SHARE || this.post == MsgItem.MSG_TYPE_RED_PACKAGE){
			this.msg = delHTMLTag(this.msg);
		}else if(msgItem.isSystemMessageByKey()){
			this.msg = msgItem.parseAttachmentId(null,"",StringUtils.isNotEmpty(msgItem.translateMsg),false);
		}else{
			if (msgItem.isShareCommentMsg()) {
				String content = "";
				if (msgItem.shareComment.equals("90200021")) {
					content = LanguageManager.getLangByKey("90200021");
				} else {
					content = msgItem.shareComment;
				}
				this.msg = StringUtils.isNotEmpty(msgItem.translateMsg) ? msgItem.translateMsg : content;
			}
			if (msgItem.isNewsCenterShare()) {
				String newsIdStr = "";
				String titleParams = "";
				String[] attachmentIDArray = msgItem.attachmentId.split("_", 3); //只分割两次,防止将名字分割了
				if (attachmentIDArray.length == 3) {
					newsIdStr = attachmentIDArray[1];
					titleParams = attachmentIDArray[2];
				}
				msgItem.msg = JniController.getInstance().excuteJNIMethod("getNewsCenterShowMsg", new Object[]{newsIdStr, titleParams});
				this.msg = msgItem.msg;
			}
		}
		if (ChatServiceController.isNeedReplaceBadWords()) {
			this.msg = FilterWordsManager.replaceSensitiveWord(this.msg, 1, "*");
		}
	}


	public static String delHTMLTag(String htmlStr){
		String regEx_html="<[^>]+>"; //定义HTML标签的正则表达式

		Pattern p_html=Pattern.compile(regEx_html,Pattern.CASE_INSENSITIVE);
		Matcher m_html=p_html.matcher(htmlStr);
		htmlStr=m_html.replaceAll(""); //过滤html标签

		return htmlStr.trim(); //返回文本字符串
	}
}
