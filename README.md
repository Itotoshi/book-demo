APIはセッション認証+CSRF対策されています。（ログインのあるWebページ上からリクエストされる想定）<br>

APIテストツールで呼び出す場合は以下の手順でログイン認証やCSRFトークン認証が必要です。<br>
※Postmanを想定しています
### 1.サーバーの準備
導入を完了し、サーバーを起動してください

### 2.コレクションの作成
<img width="1900" height="915" alt="image" src="https://github.com/user-attachments/assets/64f613ba-36c4-4643-91db-952c2888b142" />
これによりコレクション内のリクエストではクッキーが共有されます

### 3.CSRFトークンの取得
<img width="1469" height="462" alt="image" src="https://github.com/user-attachments/assets/d2cb8d7d-1d67-48fe-beb5-312db62eb0a1" />
GET<br>
localhost:8080/login<br>
↓<br>
送信

<img width="1486" height="391" alt="image" src="https://github.com/user-attachments/assets/9b83b408-ae19-42d6-830b-f3e41abc586d" />
レスポンスHTMLの27行目付近に&lt;input name="_csrf" ...&gt;から始まるタグがありますのでvalueの値をコピーします。<br>
（画像であれば_e03GyUfrDVSMcrxOty1EY3hI97sbZ_kjXrtiRftICX-F49Yz9sOIxF7zQ1_Vf7FWfGBd7qCDrzZC6rJ6UzasCDfRkfHIbw-）

### 4.ログイン実行
<img width="406" height="338" alt="image" src="https://github.com/user-attachments/assets/f781e72f-2a35-4041-8382-5bf6af000e35" /><br>
リクエストを追加します<br>

<img width="1481" height="433" alt="image" src="https://github.com/user-attachments/assets/1ed22969-5006-4995-9551-bd6da5df7f05" />
POST<br>
localhost:8080/login<br>
ボティタブを開いて以下を入力<br>

| キー  | 値 |
| ------------- | ------------- |
| _csrf | <手順3でコピーしたトークン>  |
| username  | admin  |
| password  | password  |

↓<br>
送信

<img width="1477" height="397" alt="image" src="https://github.com/user-attachments/assets/fbc03a49-e5e2-4121-bd92-132635085968" />
ページ名がindexのHTMLが取得できればログイン認証は成功です。クッキーを削除しない限りはログイン認証は不要になります。<br>
新しいCSRFトークンが払い出されているので手順3と同様にコピーしておきます。

### 5.APIの呼び出し
新しいリクエストを作成します。<br>
<img width="1462" height="231" alt="image" src="https://github.com/user-attachments/assets/9026b892-d51a-4d42-9103-6d7d52a5c6b9" />
ヘッダータブを開いて以下を入力

| キー  | 値 |
| ------------- | ------------- |
| X-XSRF-TOKEN | <手順4でコピーしたトークン>  |

<img width="1477" height="859" alt="image" src="https://github.com/user-attachments/assets/64000c82-6f29-4ffe-b176-821e7b3da057" />
あとは呼び出したい機能に応じてにメソッド(GET,POST等)、APIパスと「ボディタブ > Raw JSON」を設定すれば呼び出し可能です<br>

### API一覧

#### 著者登録
POST
localhost:8080/api/authors
```
{
  "name":"なまえ",
  "birthday":"2020-01-02"
}
```

#### 著者更新
PATCH
localhost:8080/api/authors/{id}
```
{
  "name":"なまえ",
  "birthday":"2020-01-02"
}
```
※パラメータは任意の1つ以上が必要
