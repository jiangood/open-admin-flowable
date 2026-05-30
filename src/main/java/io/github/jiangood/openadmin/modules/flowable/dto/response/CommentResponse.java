package io.github.jiangood.openadmin.modules.flowable.dto.response;

import io.github.jiangood.openadmin.util.SpringTool;
import io.github.jiangood.openadmin.modules.flowable.service.ProcessService;
import lombok.Data;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.flowable.engine.task.Comment;

@Data
public class CommentResponse {

    String id;
    String content;

    String time;

    String user;

    public CommentResponse(Comment comment) {
        this.id = comment.getId();
        this.content = comment.getFullMessage();
        this.time = DateFormatUtils.format(comment.getTime(), "yyyy-MM-dd HH:mm:ss");
        this.user = SpringTool.getBean(ProcessService.class).getUserName(comment.getUserId());
    }
}
