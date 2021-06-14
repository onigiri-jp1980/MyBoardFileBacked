package models;

import java.io.*;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.sun.corba.se.spi.activation._ServerStub;
import play.api.Environment;
import play.api.Play;
import play.api.Play;
import play.mvc.*;
import play.*;

public class MyBoardFileBackedDAO {
    final String SEPARATOR = "<>";
    final public String DATA_FILE_PATH = "data/posts.dat";
    public File dataFile = new File(getClass().getClassLoader().getResource(DATA_FILE_PATH).getFile());


    public MyBoardFileBackedDAO() {
    }

    //　データファイルから投稿データを読み込む
    public List<MyBoardFileBacked> loadPosts(File _file) {
        List<MyBoardFileBacked> posts = new ArrayList<MyBoardFileBacked>();
        try (BufferedReader br = new BufferedReader(new FileReader(_file))) {
            String str;
            while ((str = br.readLine()) != null) {
                MyBoardFileBacked mb = new MyBoardFileBacked();
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss.SSS");
                String[] splited = str.split("<>");
                mb.id = Integer.parseInt(splited[0]);
                mb.name = splited[1];
                mb.email = splited[2];
                mb.message = unEscapeCrLr(splited[3]);
                try {
                    mb.postedAt = new Timestamp(sdf.parse(splited[4]).getTime());
                    mb.updatedAt = new Timestamp(sdf.parse(splited[5]).getTime());
                } catch (ParseException e) {
                    e.printStackTrace();
                } finally {
                    posts.add(mb);
                }
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            posts = null;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return posts;

    }

    // 保持している投稿データをデータファイルに保存する
    private void saveDataFile(File _file, List<MyBoardFileBacked> _posts) {
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(_file));
            for (MyBoardFileBacked post : _posts) {
                writer.write(parsePostToLine(post));
                writer.newLine();
            }
            writer.flush();
            writer.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // 指定の投稿番号をロードした投稿データから削除しデータファイルに保存する
    public void deletePost(int _id) {
        List<MyBoardFileBacked> posts = loadPosts(this.dataFile);
        //IntelliJにラムダ式で記述するように推奨されたので：
        //for (int i=0; i < loadedPosts.size(); i++) {
        //    if (loadedPosts.get(i).getId() == _id) {
        //       loadedPosts.remove(i);
        //   }
        //}
        posts.removeIf(post -> (post.id == _id));
        saveDataFile(this.dataFile, posts);
    }

    //読み込み済みの投稿データを返す
    public List<MyBoardFileBacked> selectAll() {
        List<MyBoardFileBacked> posts = loadPosts(this.dataFile);
        return posts;
    }

    public MyBoardFileBacked select(int _id) {
        MyBoardFileBacked mb = new MyBoardFileBacked();
        List<MyBoardFileBacked> posts = loadPosts(this.dataFile);

        for (MyBoardFileBacked post : posts) {
            if (post.id == _id) {
                mb = post;
            }
        }
        return mb;
    }

    // 新規の投稿をデータファイルに追記する
    public void insertPost(MyBoardFileBacked _post) {
        Timestamp now = getCurrentTimestamp("yyyy/MM/dd hh:mm:ss.SSS");
        List<MyBoardFileBacked> loadedPosts = new ArrayList<MyBoardFileBacked>();
        loadedPosts = loadPosts(this.dataFile);
        _post.id = (getLatestId(loadedPosts));
        _post.updatedAt = now;
        _post.postedAt = now;
        loadedPosts.add(_post);
        saveDataFile(this.dataFile, loadedPosts);
    }
    public void updatePost(MyBoardFileBacked _post) {
        Timestamp now =getCurrentTimestamp("yyyy/MM/dd hh:mm:ss.SSS");
        List<MyBoardFileBacked> loadedPosts = loadPosts(this.dataFile);
        int i = 0 ;
        while(!loadedPosts.get(i).id.equals(_post.id)) {
            i++;
        }
        _post.postedAt=(loadedPosts.get(i).postedAt);
        _post.updatedAt=(now);
        loadedPosts.set(i, _post);
        saveDataFile( this.dataFile, loadedPosts );
    }


        // 投稿データを1行にパースする
        private String parsePostToLine (MyBoardFileBacked mb){
            return mb.id + SEPARATOR
                    + mb.name + SEPARATOR
                    + mb.email + SEPARATOR
                    + escapeCrLr(mb.message) + SEPARATOR
                    + mb.postedAt + SEPARATOR
                    + mb.updatedAt;
        }


        //エスケープした本文の改行を復元(View側でbrタグに)
        private String unEscapeCrLr (String _str){

            String regex = "\\\\r\\\\n|\\\\r|\\\\n";
            return _str.replaceAll(regex, "\r\n");
        }

        //本文の改行をエスケープ
        private String escapeCrLr (String _str){

            String regex = "\r\n|\r|\n";
            return _str.replaceAll(regex, "\\\\r\\\\n");
        }

        // 引数のフォーマット文字列で現在時刻をパースしたTimetampを返す
        private Timestamp getCurrentTimestamp(String _fmt){
            // String parsedCurrentTime = new SimpleDateFormat(_fmt).format(new Timestamp(System.currentTimeMillis()));
            return new Timestamp(strToDate(new SimpleDateFormat(_fmt).format(new Timestamp(System.currentTimeMillis())), _fmt).getTime());

        }
        // 引数のフォーマット文字列に従って、文字列型の時刻データをDateオブジェクトにパースして返す。
        // getCurrentTimestamp()とセット
        private Date strToDate(String _str, String _fmt){
            SimpleDateFormat sdf = new SimpleDateFormat(_fmt);
            sdf.setLenient(false);
            try {
                return sdf.parse(_str);
            } catch (ParseException e) {
                return null;
            }
        }
        // 投稿番号を採番する
        private int getLatestId (List <MyBoardFileBacked> _posts) {
            int finalId = 0;

            //最終の投稿番号を検索
            for (MyBoardFileBacked post : _posts) {
                if (finalId < post.id) {
                    finalId = post.id;
                }
            }
            //検索した最終番号に1加算した値を返却
            return finalId + 1;
        }
    }


