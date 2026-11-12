# Button 和 TextView 入门

这份文档只讲当前这个示例页面。你不需要先懂 Android，按顺序看就行。

建议打开 Android Studio，左边找到下面三个文件，和文档对着看：

- `app/src/main/res/layout/activity_main.xml`：屏幕上有什么、长什么样
- `app/src/main/res/values/strings.xml`：按钮和文字上写的字
- `app/src/main/java/com/example/demo/MainActivity.java`：点下去之后发生什么

先把 App 跑起来。点 Android Studio 上方的绿色三角，装到手机或模拟器上。看一遍屏幕，再回来读文档。

---

## 1. 屏幕上从上到下是什么

打开后你会看到这些东西，顺序和布局文件里的顺序一样：

1. 一行标题：`Button 与 TextView`
2. 一行状态文字。刚打开时会显示一句从程序里填进来的话
3. 一个居中的大号数字 `0`
4. 一句普通的话，其中「加粗」两个字是粗体，「变色」两个字是粉色
5. 一句很长的话，只显示一行，后面是省略号
6. 一句带下划线的话，点「点这里」会弹出提示
7. 五个按钮

前 6 个都是 **TextView**，意思是「用来显示文字的控件」。

后 5 个都是 **Button**，意思是「用来点击的控件」。

---

## 2. 这个页面为什么要分成两个文件

手机上的界面分成两部分来写：

- **XML 文件**负责摆放。它决定有几个文字、几个按钮、字有多大、颜色是什么、间距是多少。
- **Java 文件**负责动作。它决定按钮被点击之后，去改哪一行字、数字加几、要不要把某段文字藏起来。

可以这样记：

- XML 是一张摆好家具的房间图
- Java 是人走进房间后做的事

`strings.xml` 是第三份清单，专门存放要显示的句子。XML 和 Java 都用名字去取这些句子，而不是把句子直接写死在各处。这样以后改一句话，只改这一个文件。

---

## 3. 先认识 8 个词

后面会反复出现这几个词。现在先知道它们是什么，细节后面遇到再看。

**Activity**  
一个屏幕对应的程序页面。这个示例的页面类叫 `MainActivity`。App 打开时，系统会自动调用它里面的 `onCreate` 方法。`onCreate` 就是「这个页面第一次被创建时要做的事」。

**控件**  
屏幕上一块能看见的东西。文字是控件，按钮也是控件。Android 里控件的总称是 View。

**TextView**  
显示文字的控件。它自己不会因为被点击就改变内容，除非 Java 里写了点击后要做的事。

**Button**  
按钮。外观和 TextView 不同，主要用来被点击。

**id**  
给控件起的名字。布局里写 `android:id="@+id/titleText"`，Java 里就用 `binding.titleText` 找到这一个控件。名字必须对得上。

**资源**  
放在 `res` 文件夹里的内容。句子在 `res/values/strings.xml`，颜色在 `res/values/colors.xml`，界面在 `res/layout/`。代码里用 `R.string.某某`、`R.color.某某` 去取它们。

**dp 和 sp**  
两种尺寸单位。

- 宽度、高度、间距用 **dp**。它会按屏幕密度换算，不同手机上看到的物理大小比较接近。
- 字号用 **sp**。它和 dp 类似，还会跟随用户在系统里设置的字体大小。

**监听器**  
「当某件事发生时，请执行这段代码」。按钮最常见的是点击监听器：`setOnClickListener`。

---

## 4. XML 的一行是什么意思

打开 `activity_main.xml`，你会看到很多尖括号。先看最小的一块：

```xml
<TextView
    android:id="@+id/titleText"
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:text="@string/demo_title"
    android:textSize="22sp" />
```

读法：

- `<TextView` 表示「这里放一个文字控件」
- 中间每一行都是 `属性名="值"`
- `/>` 表示这个控件写完了

`android:` 是这些属性的固定前缀，先不用深究，照着写即可。

最外层是 `ScrollView`。内容比屏幕高时，手指可以上下滑动。里面套了一个 `LinearLayout`，`orientation="vertical"` 表示里面的控件从上往下排。

`padding="20dp"` 是这个布局的内边距，文字和按钮不会贴着屏幕边缘。

---

## 5. 第一个 TextView：标题

对应布局里的 `titleText`。

| 属性 | 值 | 意思 |
| --- | --- | --- |
| `android:id` | `@+id/titleText` | 给它起名 `titleText`。`@+id` 表示「如果还没有这个名字，就新建一个」 |
| `layout_width` | `match_parent` | 宽度和父布局一样，也就是占满这一行 |
| `layout_height` | `wrap_content` | 高度包住文字，文字有多高，控件就多高 |
| `android:text` | `@string/demo_title` | 显示 `strings.xml` 里名叫 `demo_title` 的那句字 |
| `textColor` | `@color/purple_700` | 使用 `colors.xml` 里的紫色 |
| `textSize` | `22sp` | 字号 22 |
| `textStyle` | `bold` | 整句加粗 |

`strings.xml` 里对应的是：

```xml
<string name="demo_title">Button 与 TextView</string>
```

`name` 是代号，尖括号中间的内容才是屏幕上看到的字。

你可以现在改一个字试试：把 `Button 与 TextView` 改成你的名字，重新运行，标题会跟着变。只改 XML 里的 `android:text` 也行，但这个项目把句子集中放在 `strings.xml`。

---

## 6. 后面几个 TextView，只看和标题不同的地方

### 状态文字 `statusText`

它和标题差不多，多了一个 `layout_marginTop="12dp"`，表示和上面的标题隔开 12dp。

XML 里先写了「等待操作」。页面真正出现时，Java 会立刻用另一句话把它换掉。所以你运行后看到的，通常不是「等待操作」。那句 XML 文字主要给预览用，也作为还没执行 Java 之前的初始内容。

### 大号数字 `counterText`

多了 `android:gravity="center"`。  
`match_parent` 让这一行占满屏幕宽度，`gravity="center"` 让文字在这一行里面居中。

数字本身写在 `strings.xml`：

```xml
<string name="counter_initial">0</string>
```

### 带样式的那一句 `styledText`

XML 里没有 `android:text`。这句话是 Java 的 `applyStyledText()` 放进去的，因为同一句里要让某几个字变粗、某几个字变色，XML 的 `textStyle` 只能把整句一起加粗。

`lineSpacingExtra="4dp"` 是行和行之间再多留一点空隙。这句话现在只有一行，效果不明显。

### 带省略号的那一句 `ellipsisText`

两个属性一起起作用：

- `maxLines="1"`：最多显示 1 行
- `ellipsize="end"`：放不下的部分在末尾变成 `...`

把 `maxLines` 改成 `3` 再运行，你会看到更多文字。如果改得足够大，省略号会消失。

### 能点击的那一句 `linkText`

XML 只设置了颜色和字号。哪几个字能点、点了做什么，都在 Java 的 `applyClickableText()` 里。这一段放在文档最后讲。

---

## 7. 五个按钮

按钮上也有 `id`、宽高和 `android:text`，读法和 TextView 一样。差别是按钮主要等着被点击。

这个页面用了两种按钮：

- `com.google.android.material.button.MaterialButton`：Material 样式的按钮，看起来更圆、更现代
- `Button`：Android 自带的普通按钮

它们的点击写法相同。外观不同，是因为有的写了 `style`：

| 按钮 id | 样子 | 点击后做什么 |
| --- | --- | --- |
| `changeTextButton` | 实心，没写 style，用默认样子 | 改状态文字的内容、颜色、字号 |
| `countButton` | 实心 | 大号数字加 1 |
| `toggleButton` | 描边。style 是 `OutlinedButton` | 把样式那句话藏起来或再显示 |
| `disableButton` | 只有文字，没有底色。style 是 `TextButton` | 让最下面的按钮不能点，或恢复能点 |
| `longClickButton` | 普通 Button | 轻点弹出「普通点击」；按住一会儿弹出「长按」 |

按钮上的字同样来自 `strings.xml`，例如：

```xml
<string name="btn_change_text">修改文字</string>
```

---

## 8. Java 文件从哪里看起

打开 `MainActivity.java`。可以先跳过最上面的 `import`。那些是「这个文件要用到哪些现成的类」，像是工具的进货单。

类从这里开始：

```java
public class MainActivity extends AppCompatActivity {
```

意思是：`MainActivity` 是一个页面，它使用 AndroidX 提供的 `AppCompatActivity` 作为基础。

页面出现时，系统调用 `onCreate`。这个示例在 `onCreate` 里做三件事：

1. 把 XML 变成真正的界面
2. 给几个 TextView 填上初始文字
3. 给每个按钮登记「被点击时要运行的代码」

### 界面是怎么接上 Java 的

```java
binding = ActivityMainBinding.inflate(getLayoutInflater());
setContentView(binding.getRoot());
```

项目打开了 ViewBinding。它会根据 `activity_main.xml` 自动生成一个类 `ActivityMainBinding`。

- `inflate`：按照 XML 把控件创建出来
- `setContentView`：把这棵界面放到屏幕上
- 之后用 `binding.标题的id` 找到某个控件

所以 XML 里的 `@+id/statusText`，在 Java 里写成 `binding.statusText`。中间的下划线会被去掉，并改成驼峰。这个布局里的 id 本身就是驼峰，所以两边看起来一样。

---

## 9. 跟着一个按钮走完整个过程

拿最上面的「修改文字」按钮做例子。

Java 里是这段：

```java
binding.changeTextButton.setOnClickListener(v -> {
    binding.statusText.setText(R.string.status_clicked);
    binding.statusText.setTextColor(ContextCompat.getColor(this, R.color.teal_700));
    binding.statusText.setTextSize(18);
});
```

翻译成口语：

1. 找到名叫 `changeTextButton` 的按钮
2. `setOnClickListener`：给它登记一个点击动作
3. `v -> { ... }`：当点击发生时，执行大括号里的代码。`v` 代表被点到的那个按钮，这段代码没有再用它
4. `setText`：把 `statusText` 的文字换成 `strings.xml` 里的「文字、字号和颜色已更新」
5. `setTextColor`：把这行字的颜色改成 `colors.xml` 里的 `teal_700`
6. `setTextSize(18)`：把字号改成 18sp

`R.string.status_clicked` 就是资源代号。`R` 是 Android 自动生成的名单，里面收录了所有字符串、颜色、布局 id。

手指点下去之后，系统找到这个监听器，执行大括号里的三行，屏幕上的那行状态文字就会变。

---

## 10. 另外四个按钮各自做一件事

### 「计数 +1」

```java
count++;
binding.counterText.setText(getString(R.string.counter_value, count));
```

`count` 是 Java 里的一个整数，一开始是 0。每点一次，`count++` 让它加 1。

TextView 显示的是文字，所以要把数字填进句子里。`strings.xml` 中：

```xml
<string name="counter_value">%1$d</string>
```

`%1$d` 是一个占位符：第 1 个参数，类型是整数。`getString(..., count)` 会把它换成当前的 `count`。点三次，屏幕上就是 `3`。

如果把 `count++` 改成 `count = count + 2`，每次会加 2。

### 「隐藏样式文字」

```java
detailVisible = !detailVisible;
binding.styledText.setVisibility(detailVisible ? View.VISIBLE : View.GONE);
```

`detailVisible` 是一个布尔值，只有真和假。`!` 表示取反：原来是显示，点一下就变成隐藏。

`setVisibility` 设置控件看不看得见：

- `View.VISIBLE`：看得到，也占位置
- `View.GONE`：看不到，也不占位置，下面的内容会往上移
- `View.INVISIBLE`：看不到，但原来的空白还留着。这个示例没用它

问号那一行读作：如果 `detailVisible` 为真，就用 `VISIBLE`，否则用 `GONE`。

按钮上的字也会换。藏起来之后，按钮变成「显示样式文字」，这样你知道下一次点击会把它叫回来。

### 「禁用下方按钮」

```java
boolean enabled = !binding.longClickButton.isEnabled();
binding.longClickButton.setEnabled(enabled);
```

`isEnabled()` 询问按钮现在能不能点。`setEnabled(false)` 让它变灰，点击和长按都不再执行。再点一次「启用下方按钮」，又变回能点。

### 「点按 / 长按」

同一个按钮登记了两个动作：

- `setOnClickListener`：手指按下后很快松开
- `setOnLongClickListener`：手指按住大约半秒

短按时弹出 Toast。Toast 是屏幕底部短暂出现的小提示，不是布局里的 TextView。

长按时做了两件显示上的事：

```java
binding.statusText.setText(R.string.status_long_click);
binding.statusText.append(String.valueOf(count));
```

`setText` 先把状态文字整句换成「长按触发，当前计数 」。  
`append` 在这句话后面继续加字，这里加的是当前数字。它不会把前半句清掉。

长按监听器最后写了 `return true`。这表示「这个长按我已经处理了」。

---

## 11. 页面刚打开时，状态文字从哪来

`onCreate` 里有这一行：

```java
binding.statusText.setText(getString(R.string.status_ready, stringFromJNI()));
```

`strings.xml` 里是：

```xml
<string name="status_ready">JNI：%1$s</string>
```

`%1$s` 表示第 1 个参数是一段文字。`stringFromJNI()` 会返回那段文字，然后拼到「JNI：」后面。

`stringFromJNI` 是这个示例原有的本地方法，具体句子写在 C++ 文件里。你现在把它看成「一个会返回一句话的函数」就够了。它用来演示 `setText` 可以显示程序算出来的字符串。

---

## 12. 比较难的部分：一句话里只改几个字

如果前面都看懂了，再看这一节。没看懂也可以先跳过，不影响前面几个按钮。

方法是 `applyStyledText()`。

普通的 `setText("你好")` 整句只有一种样式。想让其中两个字变粗，需要 `SpannableString`。你可以把它理解成「一句可以在指定范围上贴标签的文字」。

步骤是：

1. 取出整句：`局部样式：这里加粗，那里变色。`
2. 用 `indexOf("加粗")` 找出「加粗」从第几个字开始
3. `setSpan` 给这个范围贴上「粗体」标签
4. 再用同样的办法给「变色」贴上「粉色」标签
5. 把贴好标签的句子交给 `styledText`

范围的规则和切字符串一样：从开始位置算起，到结束位置为止，结束位置那个字不包含在内。「加粗」是 2 个字，所以结束位置是 `boldStart + 2`。

`SPAN_EXCLUSIVE_EXCLUSIVE` 是贴标签时的一种固定写法。初学阶段记住「setSpan 的最后一个参数先这样写」即可。

`applyClickableText()` 用的是同一种办法，只是标签换成了两种：

- `ClickableSpan`：这两个字被点到时执行 `onClick`
- `UnderlineSpan`：给这两个字加下划线

最后还有一行：

```java
binding.linkText.setMovementMethod(LinkMovementMethod.getInstance());
```

只有贴上 `ClickableSpan`，TextView 还不会把手指的触摸交给它。加上这一行，点「点这里」才会弹出「点到了文字里的链接」。

---

## 13. 建议你现在改的 5 个小地方

每改一处就运行一次，看屏幕哪里变了。这样比一次读完所有代码更容易记住。

1. 在 `strings.xml` 把 `demo_title` 改成任意标题。
2. 在 `activity_main.xml` 把标题的 `22sp` 改成 `30sp`。
3. 把 `btn_count` 的文字改成「再加一次」。
4. 在 `MainActivity.java` 把 `count++` 改成 `count = count + 5`。
5. 把 `ellipsisText` 的 `maxLines="1"` 改成 `maxLines="4"`，看省略号还在不在。

改完如果运行报错，优先看改过的那一行：引号有没有成对，分号有没有丢掉，XML 标签有没有写完。

---

## 14. 这页示例里，哪些句子值得记住

- 界面写在 XML，动作写在 Java。
- `android:id` 是控件的名字，`binding.这个名字` 用来找到它。
- 要显示的句子放在 `strings.xml`，用 `@string/名字` 或 `R.string.名字` 去取。
- `setText` 改文字，`setTextColor` 改颜色，`setTextSize` 改字号。
- `setOnClickListener` 处理点击，`setOnLongClickListener` 处理长按。
- `setVisibility` 控制显示和隐藏，`setEnabled` 控制按钮还能不能点。
