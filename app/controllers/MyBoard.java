package controllers;

import play.http.HttpRequestHandler;
import play.api.Environment;
import play.api.Play;
import play.data.*;
import play.data.Form.*;
import play.data.FormFactory;
import play.data.DynamicForm;
import play.api.mvc.Request;
import play.mvc.*;
import play.mvc.Http.*;
import models.*;
import views.html.*;

import javax.inject.Inject;
import java.sql.Timestamp;
import java.util.List;
import java.util.Map;

/**
 * This controller contains an action to handle HTTP requests
 * to the application's home page.
 */
public class MyBoard extends Controller {

    @Inject
    FormFactory formFactory;

    // 投稿を受け取るための内部クラス
    public class NewPost{
        String name;
        String email;
        String message;
    }

    public Result index() {

        MyBoardFileBackedDAO mbDAO = new MyBoardFileBackedDAO();
        List<MyBoardFileBacked> posts = mbDAO.selectAll();
        return ok(views.html.index.render("top",posts));
       }

    public Result edit(Integer _id){
        MyBoardFileBackedDAO mbDAO = new MyBoardFileBackedDAO();
        return ok(views.html.edit.render(mbDAO.select(_id)));
    }
    public Result update(Http.Request request){
        DynamicForm df = formFactory.form().bindFromRequest(request);
        MyBoardFileBackedDAO mbDAO = new MyBoardFileBackedDAO();
        mbDAO.updatePost(parseDynamicFormToMB(df));
        return ok(views.html.index.render("updated", mbDAO.selectAll()));
    }
    public Result delete(Http.Request request){
        DynamicForm df = formFactory.form().bindFromRequest(request);
        MyBoardFileBackedDAO mbDAO = new MyBoardFileBackedDAO();
        mbDAO.deletePost(Integer.parseInt(df.get("id")));
        List<MyBoardFileBacked> posts = mbDAO.selectAll();
        return ok(views.html.index.render("top",posts));
    }
    public Result insert(Http.Request request){
        DynamicForm df = formFactory.form().bindFromRequest(request);
        MyBoardFileBacked mb = parseDynamicFormToMB(df);
        MyBoardFileBackedDAO mbDAO = new MyBoardFileBackedDAO();
        mbDAO.insertPost(mb);
        return index();
    }
    private MyBoardFileBacked parseDynamicFormToMB(DynamicForm _df){
        MyBoardFileBacked result = new MyBoardFileBacked();
        if ( _df.get("id") != null ) { result.id = Integer.parseInt(_df.get("id")); }
        result.name = _df.get("name");
        result.email = _df.get("email");
        result.message= _df.get("message");
        return result;
    }
}
