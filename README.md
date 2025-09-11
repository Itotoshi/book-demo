# book-demo
このリポジトリはkotlinを用いた開発デモです

## 前提条件
- Docker v28.3.3
- JDK 17
- Gradle v8.14.03
- Postman (API確認用)

## インストール＆起動
1. クローン
2. DB接続情報などを確認 src/main/resources/application.properties (デモ用のためアカウント情報を直書きしています)
3. DB用Dockerコンテナを起動 ```docker-compose up```
4. アプリ起動 ```./gradlew bootRun```

## APIデモンストレーション
APIはセッション認証+CSRF対策されています（ログインのあるWebページ上からリクエストされる想定）<br>
APIテストツールで呼び出す場合はログインやCSRFトークン認証が必要です<br>

以下の手順ですぐにAPIを呼び出せる準備済みのPostman コレクションを使用できます<br>
### 1.コレクションのフォーク
以下のリンクからコレクションをフォークします<br>
[<img src="https://run.pstmn.io/button.svg" alt="Run In Postman" style="width: 128px; height: 32px;">](https://god.gw.postman.com/run-collection/48252526-46062b95-17b9-480b-90a2-77fa39828f76?action=collection%2Ffork&source=rip_markdown&collection-url=entityId%3D48252526-46062b95-17b9-480b-90a2-77fa39828f76%26entityType%3Dcollection%26workspaceId%3D68091b2d-d973-4674-8421-82318724b3d0#?env%5BNew%20Environment%5D=W10=)

いくつかボタンを押して以下の画面が表示されれば完了です
<img width="1897" height="979" alt="image" src="https://github.com/user-attachments/assets/bc5f16a8-ba10-4aa3-9538-6f018cbe7be9" />


### 2.環境選択
画面右上部が「No Environment」となっている場合は押下して「New Environment」を選択
<img width="1851" height="920" alt="image" src="https://github.com/user-attachments/assets/d7c99ed7-772a-4ec2-9e2c-b26a3a49745b" />


### 3.ログイン、CSRFトークン取得
画面左部「1. XSRF-TOKEN取得用GET」を選択してSend
<img width="1905" height="421" alt="image" src="https://github.com/user-attachments/assets/2e37497c-67d5-4317-9ac2-39884929e804" />
同様に画面左部「2.ログイン用」を選択してSend

以上でAPIデモンストレーション前に必要な準備は完了です
今後403エラーが発生した場合はセッション切れのため、この手順を再実施してください


### 4.APIデモンストレーションの実施
画面左部からAPIを選択して、ボディパラメータを入力して送信します
<img width="1862" height="581" alt="image" src="https://github.com/user-attachments/assets/323947dd-38ba-4d88-be21-6a584b92965c" />

### API一覧
#### 著者登録(CreateAuthor)
POST
localhost:8080/api/authors
```
{
  "name":"なまえ",
  "birthday":"2020-01-02"
}
```


#### 著者更新(PatchAuthor)
PATCH
localhost:8080/api/authors/{著者id}
```
{
  "name":"なまえ",
  "birthday":"2020-01-02"
}
```
※パラメータは任意の1つ以上が必要


#### 書籍登録(CreateBook)
POST
localhost:8080/api/books
```
{
  "title":"THE 本",
  "price": 8000,
  "authorIds": [1,2],
  "status": "UNPUBLISHED"
}
```
※statusはPUBLISHED, UNPUBLISHEDを指定


#### 書籍更新(PatchBook)
PATCH
localhost:8080/api/books/{書籍id}
```
{
  "title":"THE 本",
  "price": 8000,
  "authorIds": [1,2],
  "status": "UNPUBLISHED"
}
```
※パラメータは任意の1つ以上が必要
※statusはPUBLISHED, UNPUBLISHEDを指定[


#### 著者から書籍取得(GetBooksFromAuthor)
GET
localhost:8080/api/books/by-author/{著者id}


<details>
  <summary><h3>（おまけ）準備済みコレクションを用いずにAPIを呼び出したい場合</h3></summary>
  
### 1.サーバーの準備
book-demoのインストール＆起動を完了します

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

</details>
