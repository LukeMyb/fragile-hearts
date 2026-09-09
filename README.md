# Example Mod

## Setup

For setup instructions, please see the [Fabric Documentation page](https://docs.fabricmc.net/develop/getting-started/creating-a-project#setting-up) related to the IDE that you are using.

## License

This template is available under the CC0 license. Feel free to learn from it and incorporate it in your own projects.


Minecraft 1.16.1 Fabric MOD開発環境 構築手順

【前提条件】
・JDK 21がインストールされていること
・IntelliJ IDEAがインストールされていること

【手順】

テンプレートの取得
GitHubの公式リポジトリから 1.16.1 ブランチのZIPを直接ダウンロードし、解凍します。

gradle.properties の修正
解凍したフォルダ内の gradle.properties を開き、以下の3行を1.16.1向けに書き換えます（他の行はそのまま残します）。

yarn_mappings=1.16.1+build.21
fabric_api_version=0.18.0+build.387-1.16.1

IDEでのJava設定（重要）
IntelliJ IDEAでプロジェクトを開き、以下の2箇所のJava設定を意図的に分けます。

・Gradle JVM（ビルドツール用）
ファイル → 設定 → ビルド、実行、デプロイ → ビルドツール → Gradle
Java 21を指定

・プロジェクト SDK（マイクラ用）
ファイル → プロジェクト構造 → プロジェクト
Java 8 (1.8) を指定

同期と起動
(1) Gradleの再同期（Reload All Gradle Projects）を実行します。
(2) 同期完了後、Gradleタブから Tasks → fabric → runClient を実行して起動確認を行います。