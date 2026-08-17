# 8/20 教材：泛型與 Java Collections Framework

## 單元名稱

Generic class、generic method、bounded type、Collection、List、Set、Map 與 Iterator

## 課程定位

前一單元以 interface 與 polymorphism 建立可替換設計。本單元進一步使用 generic type，讓同一個 class、interface 或 method 在保留型態安全的前提下處理不同資料型態，並認識 Java Collections Framework 的核心階層。

## 學習目標

完成本單元後，應能：

1. 說明 generic 解決的型態安全問題。
2. 建立 generic class 與 generic method。
3. 使用 bounded type 限制可接受型態。
4. 區分 `Collection` hierarchy 與 `Map` hierarchy。
5. 依需求選擇 `List`、`Set` 或 `Map`。
6. 使用 interface type 宣告集合變數。
7. 使用 `Iterator` 安全走訪與刪除資料。

## 先備知識

- Class、interface、inheritance 與 polymorphism。
- 陣列、迴圈、object reference。
- `ArrayList` 基本新增與走訪經驗。
- 建立 `0820` 資料夾，所有範例依標示檔名存放。

## 問題情境

如果一個容器使用 `Object` 保存資料，它可以放入任何 object，但取出時需要強制轉型，而且錯誤可能到執行時才出現。Generic 把「允許保存的型態」變成 class 或 method 的參數，讓編譯器在程式執行前檢查型態。

Java Collections Framework 建立在 generic 與 interface 之上。`List<String>` 表示這是一個保存 `String`、具有順序與索引的集合；實際 implementation 可以依操作需求選擇 `ArrayList` 或 `LinkedList`。

## 核心概念

### 概念 1：為什麼需要 Generic

#### 概念說明

Generic 讓 type 成為 class、interface 或 method 的參數。`Box<String>` 中的 `String` 是 type argument，表示這個 box 只能保存字串。

沒有 generic 時，若使用 `Object` 保存資料，取出後通常要 cast。錯誤型態可能在很遠的地方造成 `ClassCastException`。使用 generic 後，編譯器會在加入錯誤型態時直接拒絕。

Generic 不能直接使用 primitive type，因此 `int` 使用 wrapper class `Integer`，`double` 使用 `Double`。

#### 實際應用

- `ArrayList<String>` 保存姓名。
- `HashMap<String, Product>` 以商品編號取得商品。
- `TreeNode<Integer>` 建立整數樹節點。

#### 資料變化

```text
Box<String> -> set("Java")  合法
Box<String> -> set(100)     編譯錯誤
```

#### 何時適合使用

同一種資料結構或演算法需要處理多種 reference type，而且操作邏輯相同時使用 generic。

#### 範例程式

檔名：`GenericBoxDemo.java`

```java
class Box<T> {
    private T value;

    void set(T value) {
        this.value = value;
    }

    T get() {
        return value;
    }

    boolean isEmpty() {
        return value == null;
    }
}

public class GenericBoxDemo {
    public static void main(String[] args) {
        Box<String> textBox = new Box<>();
        Box<Integer> numberBox = new Box<>();

        textBox.set("Java");
        numberBox.set(114);

        System.out.println(textBox.get().toUpperCase());
        System.out.println(numberBox.get() + 1);
        System.out.println("textBox empty：" + textBox.isEmpty());
    }
}
```

執行結果：

```text
JAVA
115
textBox empty：false
```

#### 執行重點

`T` 在建立物件時分別被視為 `String` 與 `Integer`，取出後不需要手動 cast。

---

### 概念 2：Generic class 可以有多個 type parameter

#### 概念說明

Generic class 可以宣告多個 type parameter，例如 `Pair<K, V>`。慣例名稱包括：

- `T`：Type
- `E`：Element
- `K`：Key
- `V`：Value
- `N`：Number

名稱只是慣例，重要的是每個 type parameter 的角色必須清楚。

#### 實際應用

- Key-value pair。
- Graph edge 保存起點型態與權重型態。
- 查詢結果同時保存資料與狀態。

#### 資料變化

`Pair<String, Integer>` 建立後，key 固定為 `String`，value 固定為 `Integer`。

#### 何時適合使用

一個結構包含兩種以上彼此獨立的型態角色時使用。若型態固定且沒有重用需求，普通 class 會更直接。

#### 範例程式

檔名：`GenericPairDemo.java`

```java
class Pair<K, V> {
    private K key;
    private V value;

    Pair(K key, V value) {
        this.key = key;
        this.value = value;
    }

    K getKey() {
        return key;
    }

    V getValue() {
        return value;
    }

    @Override
    public String toString() {
        return key + " -> " + value;
    }
}

public class GenericPairDemo {
    public static void main(String[] args) {
        Pair<String, Integer> score = new Pair<>("Amy", 92);
        Pair<Integer, String> course = new Pair<>(101, "Data Structures");

        System.out.println(score);
        System.out.println(course);
        System.out.println(score.getValue() + 8);
    }
}
```

執行結果：

```text
Amy -> 92
101 -> Data Structures
100
```

#### 執行重點

兩個 `Pair` 使用相同 class，但 `K`、`V` 的實際型態與順序不同。

---

### 概念 3：Generic method

#### 概念說明

Generic method 宣告自己的 type parameter，寫在回傳型態之前：

```java
static <T> T first(T[] data)
```

Method 所在的 class 不必是 generic class。呼叫時通常由 compiler 根據參數推論 `T`，不需要手動指定。

#### 實際應用

- 顯示任意型態陣列。
- 交換陣列元素。
- 從任意 object array 取得第一筆資料。

#### 資料變化

同一個 `printArray()` 可以接收 `String[]` 與 `Integer[]`，但每次呼叫中的 `T` 保持一致。

#### 何時適合使用

只有單一或少數 method 需要型態參數，而整個 class 不需要保存該型態狀態時使用 generic method。

#### 範例程式

檔名：`GenericMethodDemo.java`

```java
public class GenericMethodDemo {
    static <T> void printArray(T[] data) {
        for (T value : data) {
            System.out.print(value + " ");
        }
        System.out.println();
    }

    static <T> T first(T[] data) {
        if (data == null || data.length == 0) {
            return null;
        }
        return data[0];
    }

    public static void main(String[] args) {
        String[] names = {"Amy", "Ben", "Cara"};
        Integer[] scores = {82, 75, 91};

        printArray(names);
        printArray(scores);
        System.out.println("第一個名字：" + first(names));
        System.out.println("第一個分數：" + first(scores));
    }
}
```

執行結果：

```text
Amy Ben Cara 
82 75 91 
第一個名字：Amy
第一個分數：82
```

#### 執行重點

`T[]` 不能接收 primitive array，例如 `int[]`；此處使用 `Integer[]`。

---

### 概念 4：Bounded type 限制可用型態

#### 概念說明

有些 generic 演算法不是任何 object 都適用。例如計算總和需要 `intValue()` 或 `doubleValue()`，可以將 type parameter 限制為 `Number` 的 subtype：

```java
<T extends Number>
```

Generic 中的 `extends` 同時用於 class 與 interface upper bound。如果要限制可比較型態，可以寫成 `<T extends Comparable<T>>`。

#### 實際應用

- 對 `Integer`、`Double` 等數值計算總和。
- 要求 tree 中的資料可以比較大小。
- 限制 generic service 只接受特定父型態。

#### 資料變化

`sum(Integer[])` 與 `sum(Double[])` 合法；`sum(String[])` 在編譯階段失敗。

#### 何時適合使用

Generic 內部必須呼叫某個父型態或 interface 的 method 時，使用 bound 明確說明需求。

#### 範例程式

檔名：`BoundedGenericDemo.java`

```java
public class BoundedGenericDemo {
    static <T extends Number> double sum(T[] data) {
        double total = 0;
        for (T value : data) {
            total += value.doubleValue();
        }
        return total;
    }

    static <T extends Comparable<T>> T max(T first, T second) {
        return first.compareTo(second) >= 0 ? first : second;
    }

    public static void main(String[] args) {
        Integer[] integers = {10, 20, 30};
        Double[] doubles = {1.5, 2.5, 3.0};

        System.out.println("整數總和：" + sum(integers));
        System.out.println("小數總和：" + sum(doubles));
        System.out.println("較大字串：" + max("Java", "Graph"));
    }
}
```

執行結果：

```text
整數總和：60.0
小數總和：7.0
較大字串：Java
```

#### 執行重點

`String` 不是 `Number`，不能傳給 `sum()`，但它實作 `Comparable<String>`，可以傳給 `max()`。

---

### 概念 5：Collections Framework 的 interface 與 implementation

#### 概念說明

Collections Framework 是一組用來儲存與操作記憶體中資料的 interface、implementation 與演算法。主要分成：

```text
Iterable
└── Collection
    ├── List
    ├── Set
    └── Queue / Deque

Map  另一套 key-value hierarchy，不是 Collection 的 subtype
```

宣告變數時通常使用 interface type：

```java
List<String> names = new ArrayList<>();
```

這讓程式依賴 `List` 提供的行為，而不是綁死在特定 implementation。

#### 實際應用

- `List`：有順序、可重複、可用 index。
- `Set`：不允許重複，通常不提供 index。
- `Map`：使用 unique key 對應 value。
- `Queue`：依處理順序加入與取出資料。

#### 資料變化

```text
List<String> names = new ArrayList<>()
interface type              implementation object
```

Caller 依賴 List 宣告的操作，後續可將 implementation 改成 LinkedList，不需要修改大多數使用端程式。

#### 何時適合使用

先決定需要哪種行為，再選 implementation。不要因為最熟悉 `ArrayList` 就把所有問題都放進 `ArrayList`。

#### 範例程式

檔名：`CollectionHierarchyDemo.java`

```java
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class CollectionHierarchyDemo {
    static void printCollection(String label, Collection<String> data) {
        System.out.println(label + " size=" + data.size() + " " + data);
    }

    public static void main(String[] args) {
        List<String> list = new ArrayList<>();
        list.add("Tree");
        list.add("Graph");
        list.add("Tree");

        Set<String> set = new HashSet<>();
        set.add("Tree");
        set.add("Graph");
        set.add("Tree");

        printCollection("List", list);
        printCollection("Set", set);
        System.out.println("List index 1：" + list.get(1));
    }
}
```

執行結果中的 `HashSet` 順序不保證固定，但內容與數量應符合：

```text
List size=3 [Tree, Graph, Tree]
Set size=2 [Graph, Tree]
List index 1：Graph
```

#### 執行重點

`printCollection()` 可以接收 `List` 與 `Set`，因為兩者都是 `Collection`。`get(index)` 只屬於 `List`，不能對一般 `Collection` 呼叫。

---

### 概念 6：List、Set 與 Map 的資料語意

#### 概念說明

選擇集合時先判斷資料語意：

| 問題 | 建議介面 |
|---|---|
| 需要保留加入順序與重複資料 | `List` |
| 只關心是否存在，且不允許重複 | `Set` |
| 需要由唯一 key 快速找到 value | `Map` |

`Map<K, V>` 的 key 不可重複。再次 `put()` 相同 key 會替換原 value，並回傳被取代的 value。

#### 實際應用

- `List<Order>` 保存訂單處理順序。
- `Set<String>` 保存不重複的標籤。
- `Map<String, Product>` 由商品編號查詢商品。

#### 資料變化

```text
map.put("S101", 80) -> S101=80
map.put("S101", 95) -> S101=95，舊值 80 被替換
```

#### 何時適合使用

若程式經常用 key 搜尋資料，不要每次用 List 從頭掃描。若資料允許重複且順序重要，也不要誤用 Set。

#### 範例程式

檔名：`ListSetMapDemo.java`

```java
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ListSetMapDemo {
    public static void main(String[] args) {
        List<String> visitHistory = new ArrayList<>();
        Set<String> uniquePages = new HashSet<>();
        Map<String, Integer> pageCounts = new HashMap<>();

        String[] pages = {"Home", "Tree", "Home", "Graph", "Tree"};

        for (String page : pages) {
            visitHistory.add(page);
            uniquePages.add(page);
            pageCounts.put(page, pageCounts.getOrDefault(page, 0) + 1);
        }

        System.out.println("歷程：" + visitHistory);
        System.out.println("不重複頁面：" + uniquePages);
        System.out.println("Home 次數：" + pageCounts.get("Home"));
        System.out.println("Graph 次數：" + pageCounts.get("Graph"));
    }
}
```

可能的執行結果：

```text
歷程：[Home, Tree, Home, Graph, Tree]
不重複頁面：[Graph, Tree, Home]
Home 次數：2
Graph 次數：1
```

#### 執行重點

三個集合接收相同資料，但分別回答「依序發生什麼」、「有哪些不同項目」、「每項出現幾次」。Set 的輸出順序不可作為程式規則。

---

### 概念 7：Iterator 與走訪中安全刪除

#### 概念說明

Enhanced `for` loop 底層使用 Iterator。若走訪時直接呼叫 collection 的 `remove()` 改變結構，通常會觸發 `ConcurrentModificationException`。

需要一邊走訪一邊刪除目前元素時，應明確取得 `Iterator`，使用：

1. `hasNext()` 確認是否還有資料。
2. `next()` 取得下一筆。
3. `iterator.remove()` 刪除剛才取得的資料。

#### 實際應用

- 移除過期工作項目。
- 清除低於門檻的成績。
- 過濾無效輸入資料。

#### 資料變化

```text
[12, 35, 8, 50]
讀到 12 -> remove -> [35, 8, 50]
讀到 35 -> 保留
讀到 8  -> remove -> [35, 50]
```

#### 何時適合使用

必須修改原集合且刪除條件需要逐筆判斷時使用 Iterator。若只要建立新的過濾結果，可以新增另一個集合，避免修改原資料。

#### 範例程式

檔名：`IteratorRemovalDemo.java`

```java
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class IteratorRemovalDemo {
    public static void main(String[] args) {
        List<Integer> scores = new ArrayList<>();
        scores.add(12);
        scores.add(35);
        scores.add(8);
        scores.add(50);

        Iterator<Integer> iterator = scores.iterator();
        while (iterator.hasNext()) {
            int score = iterator.next();
            if (score < 20) {
                iterator.remove();
            }
        }

        System.out.println(scores);
    }
}
```

執行結果：

```text
[35, 50]
```

#### 執行重點

`remove()` 必須在成功呼叫 `next()` 後使用，且不能對同一筆資料連續呼叫兩次。

---

### 概念 8：Raw type 與 compile-time type safety

#### 概念說明

`List<String>` 的 type argument 是 `String`，compiler 可以阻止將 `Integer` 加入名單。如果寫成沒有 type argument 的 `List`，就是 raw type。Raw type 主要是為了與早期 Java 程式相容，新程式不應該使用。

Raw list 可以放入任意 object，取出時只得到 `Object`。開發者必須自己 cast，型態錯誤就會從編譯階段延後到 runtime，形成 `ClassCastException`。

#### 實際應用

- 讀取舊程式時辨識 unchecked warning。
- 設計 repository 或 utility 時保留具體型態。
- Code review 時找出不安全 cast 與 raw collection。

#### 資料變化

```text
List raw = new ArrayList();
raw.add("Amy");
raw.add(100);
String value = (String) raw.get(1);  // runtime 失敗
```

Generic version 會在 `names.add(100)` 當下就發現型態錯誤，不會讓錯誤資料進入 collection。

#### 設計判斷

`List<Object>` 不是 raw type。它明確表示可放入任意 object，取出時也明確得到 `Object`。Raw `List` 則放棄了 generic type checking，意義和風險都不同。

#### 範例程式

檔名：`RawTypeSafetyDemo.java`

```java
import java.util.ArrayList;
import java.util.List;

public class RawTypeSafetyDemo {
    @SuppressWarnings({"rawtypes", "unchecked"})
    static void rawTypeExample() {
        List raw = new ArrayList();
        raw.add("Amy");
        raw.add(100);

        try {
            String value = (String) raw.get(1);
            System.out.println(value);
        } catch (ClassCastException exception) {
            System.out.println("raw type error: Integer cannot become String");
        }
    }

    static void genericExample() {
        List<String> names = new ArrayList<>();
        names.add("Amy");
        names.add("Ben");
        System.out.println(names);
    }

    public static void main(String[] args) {
        rawTypeExample();
        genericExample();
    }
}
```

執行結果：

```text
raw type error: Integer cannot become String
[Amy, Ben]
```

#### 實作變化

移除 `@SuppressWarnings`，使用 `javac -Xlint:unchecked RawTypeSafetyDemo.java` 編譯，記錄 compiler 指出的 warning 位置，再將 raw list 改成 `List<String>`。

---

### 概念 9：Wildcard 與 PECS

#### 概念說明

Generic type 預設是 invariant。即使 `Integer` 是 `Number` 的 subtype，`List<Integer>` 並不是 `List<Number>` 的 subtype。否則 method 可能透過 `List<Number>` 加入 `Double`，破壞原本的 `List<Integer>`。

Wildcard 用來表示「某個尚未確定的 type」：

- `List<?>`：可以安全讀成 `Object`，不能新增一般物件。
- `List<? extends Number>`：可以讀取 `Number`，適合 producer。
- `List<? super Integer>`：可以寫入 `Integer`，適合 consumer。

PECS 是記憶原則：Producer Extends, Consumer Super。只是讀出 `T` 時考慮 `extends`；需要寫入 `T` 時考慮 `super`。

#### 實際應用

- 數值統計 method 可接收 Integer、Double 等 list。
- 將多個來源 list 複製到一個 destination。
- 設計不需要知道具體 element type 的輸出 utility。

#### 資料變化

| Method parameter | 可讀取 | 可寫入 |
|---|---|---|
| `List<?>` | `Object` | 只能 `null` |
| `List<? extends Number>` | `Number` | 不可寫入一般 Number |
| `List<? super Integer>` | `Object` | `Integer` |

#### 設計判斷

如果 method 同時要從 source 讀取並寫入 destination，可以使用兩個 parameter：`List<? extends T> source` 與 `List<? super T> destination`。不要用 raw type 解決 wildcard 設計問題。

#### 範例程式

檔名：`WildcardPecsDemo.java`

```java
import java.util.ArrayList;
import java.util.List;

public class WildcardPecsDemo {
    static double sum(List<? extends Number> values) {
        double total = 0.0;
        for (Number value : values) {
            total += value.doubleValue();
        }
        return total;
    }

    static void addDefaults(List<? super Integer> destination) {
        destination.add(60);
        destination.add(70);
    }

    static <T> void copy(List<? extends T> source,
                         List<? super T> destination) {
        for (T value : source) {
            destination.add(value);
        }
    }

    public static void main(String[] args) {
        List<Integer> scores = new ArrayList<>(List.of(80, 90));
        List<Number> numbers = new ArrayList<>();

        addDefaults(scores);
        copy(scores, numbers);

        System.out.println("scores=" + scores);
        System.out.println("numbers=" + numbers);
        System.out.println("sum=" + sum(numbers));
    }
}
```

執行結果：

```text
scores=[80, 90, 60, 70]
numbers=[80, 90, 60, 70]
sum=300.0
```

#### 實作變化

建立 `average(List<? extends Number> values)`，空 list 回傳 `0.0`，並分別使用 `List<Integer>` 與 `List<Double>` 測試。

---

### 概念 10：`Comparable`、`Comparator` 與多種排序規則

#### 概念說明

`Comparable<T>` 定義一個 class 的 natural order，class 實作 `compareTo(T other)`。例如學號是學籍資料的穩定身分，可以定義為 natural order。

`Comparator<T>` 是外部排序策略，可以為同一種 object 建立多種排序規則，例如成績由高到低、姓名字典順序。

`compareTo` 或 comparator 回傳負數、0、正數分別表示小於、等於、大於。不要假設只會回傳 `-1`、`0`、`1`。

#### 實際應用

- 成績報表依分數或姓名排序。
- 商品依價格、庫存或名稱排序。
- 排程系統依優先等級與建立時間排序。

#### 資料變化

```text
原始：[S103 75, S101 90, S102 90]
natural order：[S101 90, S102 90, S103 75]
成績降冪：[S101 90, S102 90, S103 75]
```

成績相同時再依 id 排序，可以讓結果穩定且可預測。

#### 設計判斷

Natural order 應該是 class 最穩定、最一般的順序。依報表需求改變的排序，例如成績、庫存或姓名，應使用 Comparator。如果原始順序仍有意義，排序前要建立 list copy。

#### 範例程式

檔名：`ComparableComparatorDemo.java`

```java
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

class RankedStudent implements Comparable<RankedStudent> {
    private final String id;
    private final String name;
    private final int score;

    RankedStudent(String id, String name, int score) {
        this.id = id;
        this.name = name;
        this.score = score;
    }

    int getScore() {
        return score;
    }

    String getName() {
        return name;
    }

    @Override
    public int compareTo(RankedStudent other) {
        return id.compareTo(other.id);
    }

    @Override
    public String toString() {
        return id + " " + name + " " + score;
    }
}

public class ComparableComparatorDemo {
    public static void main(String[] args) {
        List<RankedStudent> students = new ArrayList<>();
        students.add(new RankedStudent("S103", "Cara", 75));
        students.add(new RankedStudent("S101", "Amy", 90));
        students.add(new RankedStudent("S102", "Ben", 90));

        students.sort(null);
        System.out.println("by id=" + students);

        Comparator<RankedStudent> byScore =
                Comparator.comparingInt(RankedStudent::getScore)
                        .reversed()
                        .thenComparing(RankedStudent::getName);
        students.sort(byScore);
        System.out.println("by score=" + students);
    }
}
```

執行結果：

```text
by id=[S101 Amy 90, S102 Ben 90, S103 Cara 75]
by score=[S101 Amy 90, S102 Ben 90, S103 Cara 75]
```

#### 實作變化

新增第三種排序：先依姓名長度升冪，長度相同時再依姓名字典順序。

---

### 概念 11：`equals`/`hashCode` 對 Set 的影響

#### 概念說明

`HashSet` 不是單純使用 `toString()` 或比較每個 field。它先使用 `hashCode()` 找候選位置，再使用 `equals()` 確認是否為相同元素。

當兩個 object 依 `equals()` 為相等時，它們必須有相同 `hashCode()`。否則 HashSet 可能將邏輯上相等的 object 放在不同位置，破壞查找與去重結果。

#### 實際應用

- 以學號判斷重複報名。
- 以商品編號判斷是否已加入清單。
- 將 domain object 當作 HashMap key。

#### 資料變化

```text
add S101 Amy  -> true, size=1
add S101 Amy2 -> false, size=1
add S102 Ben  -> true, size=2
```

#### 設計判斷

用來判斷身分的 field 應該穩定。物件放入 HashSet 後，如果參與 `hashCode()` 的 field 被修改，後續 `contains` 或 `remove` 可能找不到該物件。

#### 範例程式

檔名：`HashSetEqualityDemo.java`

```java
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

class EnrollmentKey {
    private final String studentId;
    private final String studentName;

    EnrollmentKey(String studentId, String studentName) {
        this.studentId = studentId;
        this.studentName = studentName;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof EnrollmentKey key)) {
            return false;
        }
        return Objects.equals(studentId, key.studentId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(studentId);
    }

    @Override
    public String toString() {
        return studentId + " " + studentName;
    }
}

public class HashSetEqualityDemo {
    public static void main(String[] args) {
        Set<EnrollmentKey> enrollments = new HashSet<>();

        System.out.println(enrollments.add(
                new EnrollmentKey("S101", "Amy")));
        System.out.println(enrollments.add(
                new EnrollmentKey("S101", "Amy Chen")));
        System.out.println(enrollments.add(
                new EnrollmentKey("S102", "Ben")));
        System.out.println("size=" + enrollments.size());
    }
}
```

執行結果：

```text
true
false
true
size=2
```

#### 實作變化

將身分改成由 `studentId` 與 `courseCode` 共同決定，同一學號可以加入不同課程，但不可重複加入同一門課。

---

### 概念 12：綜合應用，課程報名、標籤與成績排序

#### 問題情境

一個課程管理系統需要保留報名順序、防止學號重複、根據學號查找資料，並可以依成績產生排名。單一 collection 無法同時最清楚地表達這些需求，因此使用 List、Set 與 Map 分工。

#### 實際應用

- 課程報名、活動報名與會員名單管理。
- 需要同時保留順序、防止重複並依 id 查詢的系統。
- 以 Comparator 產生不同報表，但不改變原始業務順序。

#### 設計分工

- `List<CourseEnrollment>`：保留報名順序與完整 object。
- `Set<String>`：快速判斷學號是否已存在。
- `Map<String, CourseEnrollment>`：根據學號取得完整資料。
- `Comparator<CourseEnrollment>`：建立排名用 copy，不破壞報名順序。

#### 資料變化

| 操作 | List | Set | Map |
|---|---|---|---|
| enroll S101 | 加入末端 | 加入 S101 | S101 -> object |
| duplicate S101 | 不改變 | `add` 回傳 false | 不改變 |
| removeBelow(60) | 移除低分 | 重建現存 id | 重建現存 mapping |

#### 設計判斷

多個 collection 存放同一批資料的不同索引時，每次新增與刪除都必須保持一致。集中在 `RegistrationBook` 的 method 中維護，不讓 caller 分別操作三個 collection。資料量很小且只有單一需求時，不需要同時建立三種結構。

#### 範例程式

檔名：`CourseRegistrationCollectionsSystem.java`

```java
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

class CourseEnrollment {
    private final String studentId;
    private final String name;
    private int score;
    private final Set<String> tags = new HashSet<>();

    CourseEnrollment(String studentId, String name, int score) {
        this.studentId = studentId;
        this.name = name;
        this.score = Math.max(0, Math.min(100, score));
    }

    String getStudentId() {
        return studentId;
    }

    int getScore() {
        return score;
    }

    void addTag(String tag) {
        if (tag != null && !tag.isBlank()) {
            tags.add(tag.toLowerCase());
        }
    }

    boolean hasTag(String tag) {
        return tag != null && tags.contains(tag.toLowerCase());
    }

    @Override
    public String toString() {
        return studentId + " " + name + " score=" + score + " tags=" + tags;
    }
}

class RegistrationBook {
    private final List<CourseEnrollment> order = new ArrayList<>();
    private final Set<String> registeredIds = new HashSet<>();
    private final Map<String, CourseEnrollment> byId = new HashMap<>();

    boolean enroll(CourseEnrollment enrollment) {
        if (enrollment == null
                || !registeredIds.add(enrollment.getStudentId())) {
            return false;
        }
        order.add(enrollment);
        byId.put(enrollment.getStudentId(), enrollment);
        return true;
    }

    CourseEnrollment find(String studentId) {
        return byId.get(studentId);
    }

    List<CourseEnrollment> ranking() {
        List<CourseEnrollment> result = new ArrayList<>(order);
        result.sort(Comparator.comparingInt(CourseEnrollment::getScore)
                .reversed()
                .thenComparing(CourseEnrollment::getStudentId));
        return result;
    }

    void removeBelow(int minimum) {
        order.removeIf(enrollment -> enrollment.getScore() < minimum);
        registeredIds.clear();
        byId.clear();
        for (CourseEnrollment enrollment : order) {
            registeredIds.add(enrollment.getStudentId());
            byId.put(enrollment.getStudentId(), enrollment);
        }
    }
}

public class CourseRegistrationCollectionsSystem {
    public static void main(String[] args) {
        RegistrationBook book = new RegistrationBook();
        CourseEnrollment amy = new CourseEnrollment("S101", "Amy", 88);
        CourseEnrollment ben = new CourseEnrollment("S102", "Ben", 55);
        CourseEnrollment cara = new CourseEnrollment("S103", "Cara", 92);

        amy.addTag("Java");
        amy.addTag("java");
        cara.addTag("Tree");

        System.out.println("enroll Amy=" + book.enroll(amy));
        System.out.println("duplicate=" + book.enroll(
                new CourseEnrollment("S101", "Amy2", 100)));
        book.enroll(ben);
        book.enroll(cara);

        System.out.println("find=" + book.find("S102"));
        System.out.println("ranking=" + book.ranking());
        book.removeBelow(60);
        System.out.println("after cleanup=" + book.ranking());
    }
}
```

執行結果（Set 中 tag 顯示順序可能不同）：

```text
enroll Amy=true
duplicate=false
find=S102 Ben score=55 tags=[]
ranking=[S103 Cara score=92 tags=[tree], S101 Amy score=88 tags=[java], S102 Ben score=55 tags=[]]
after cleanup=[S103 Cara score=92 tags=[tree], S101 Amy score=88 tags=[java]]
```

#### 實作變化

新增 `findByTag(String tag)`，回傳一個新的 `List<CourseEnrollment>`，不可把內部 `order` 直接回傳給 caller。

## 程式執行追蹤

### 追蹤一：Raw type 將錯誤延後

| 步驟 | Raw type | Generic type |
|---|---|---|
| 加入 `"Amy"` | 通過 | 通過 |
| 加入 `100` | 只出現 warning | compile error |
| 當作 String 取出 | runtime exception | 不會進入這個狀態 |

### 追蹤二：Set 去重

```text
HashSet.add(S101 Amy)
  -> hashCode(studentId)
  -> bucket 尚無相等 object
  -> add 回傳 true

HashSet.add(S101 Amy2)
  -> 相同 hashCode
  -> equals 比較 studentId 為 true
  -> add 回傳 false
```

## 除錯練習

### 除錯練習一：誤把 `List<Integer>` 當成 `List<Number>`

```java
List<Integer> scores = new ArrayList<>();
List<Number> numbers = scores;
```

這段無法編譯。如果 method 只需讀取數值，parameter 可以改成 `List<? extends Number>`。不可使用 raw type 強行跳過型態檢查。

### 除錯練習二：只 override `equals()`

當 class 只依 studentId override `equals()`，卻保留 `Object.hashCode()`，兩個邏輯上相等的 object 可能產生不同 hash code。修正方式是讓 `hashCode()` 使用與 `equals()` 一致的身分欄位。

## 課堂實作題

### 課堂實作題一：Generic Result

指定檔名：`GenericResultDemo.java`

建立 `Result<T>`，包含 `success`、`message` 與 `data`。分別建立 `Result<String>`、`Result<Integer>`，並正確處理失敗時 `data == null`。

完成標準：取出資料不需要 cast；錯誤型態在編譯階段可被發現。

### 課堂實作題二：Generic 陣列工具

指定檔名：`GenericArrayTools.java`

完成 generic method：

```java
static <T> int countMatches(T[] data, T target)
static <T> T last(T[] data)
static <T> void swap(T[] data, int first, int second)
```

必須處理 `null`、空陣列與不合法 index。

### 課堂實作題三：課程標籤統計

指定檔名：`CourseTagReport.java`

輸入一組可能重複的課程標籤，同時建立：

- `List<String>` 保存原始順序。
- `Set<String>` 保存不重複標籤。
- `Map<String, Integer>` 統計次數。

輸出三種資料並說明各自用途。

### 課堂實作題四：Wildcard 數值工具

指定檔名：`WildcardNumberTools.java`

完成：

```java
static double average(List<? extends Number> values)
static double maximum(List<? extends Number> values)
static void addRange(List<? super Integer> target, int start, int end)
```

- `average` 與 `maximum` 要可同時接收 `List<Integer>` 與 `List<Double>`。
- 空 list 的 average 回傳 `0.0`，maximum 回傳 `Double.NaN`。
- `start > end` 時 `addRange` 不加入任何資料。
- 不得使用 raw type。

### 課堂實作題五：多規則商品排序

指定檔名：`ProductComparatorPractice.java`

建立 `StoreProduct`，包含 id、name、price 與 stock。

1. Natural order 依 id 升冪。
2. Comparator 一：依 price 升冪，同價時依 name。
3. Comparator 二：依 stock 降冪，同庫存時依 id。
4. 每次排序前都要使用 `new ArrayList<>(products)` 建立 copy，保留原始順序。

至少測試五筆商品，包含同價與同庫存資料。

## 課後作業

### 課後作業一：Generic Repository

指定檔名：`GenericRepositorySystem.java`

建立 `Repository<T>`，內部使用 `ArrayList<T>`，提供 add、get、remove、size 與完整輸出。分別使用 `Repository<String>` 與 `Repository<Product>` 測試。

### 課後作業二：文字索引系統

指定檔名：`WordIndexSystem.java`

讀取程式內建句子陣列，使用 `Map<String, Integer>` 統計單字次數、`Set<String>` 保存不重複單字，並輸出出現至少兩次的單字。忽略英文大小寫與句點、逗號。

### 課後作業三：安全清理名單

指定檔名：`EnrollmentCleanup.java`

建立包含重複、空白與 `null` 資料的 `List<String>`。使用 Iterator 移除不合法資料，再使用 Set 找出重複姓名，輸出清理前、清理後及重複報告。

### 課後作業四：課程報名身分集合

指定檔名：`EnrollmentSetSystem.java`

建立 `Enrollment`，以 `studentId + courseCode` 作為身分。正確 override `equals()` 與 `hashCode()`，再使用 `HashSet<Enrollment>` 完成：

- 同一人可加入不同課程。
- 同一人不可重複加入同一課程。
- 新增與取消都輸出 boolean result。
- 以新建立但身分相同的 object 測試 `contains()` 與 `remove()`。

### 課後作業五：課程管理集合系統

指定檔名：`CourseCollectionManager.java`

以綜合案例為基礎，加入：

1. `updateScore(String studentId, int score)`。
2. `findByTag(String tag)`。
3. `scoreDistribution()` 回傳 `Map<String, Integer>`，統計 A、B、C、D、F。
4. `top(int count)` 回傳排名前 count 名，count 大於人數時回傳所有資料。
5. `removeBelow(int minimum)` 後，List、Set 與 Map 必須保持一致。

至少使用六筆報名資料，包含重複學號、同分與空白 tag。

## 常見錯誤與診斷

| 問題 | 常見原因 | 修正方式 |
|---|---|---|
| `unexpected type` | Generic 使用 primitive，例如 `List<int>` | 改用 `Integer`、`Double` 等 wrapper class |
| 取出資料需要強制轉型 | 使用 raw type，例如 `List list` | 宣告完整 generic type |
| Static method 無法直接使用 class 的 `T` | Class type parameter 屬於 instance type context | 讓 method 宣告自己的 `<T>` |
| `ClassCastException` | 使用 raw type 或不安全 cast | 移除 raw type，讓 compiler 檢查 |
| Set 顯示順序每次不同 | `HashSet` 不保證 iteration order | 不依賴順序，或明確選其他 implementation |
| `ConcurrentModificationException` | Enhanced for 中直接修改集合 | 使用 `Iterator.remove()` 或建立新集合 |
| Map 重複 key 後資料變少 | 相同 key 會替換 value | 檢查 key 是否應唯一，或改用 value collection |

## 形成性評量

1. `Box<T>` 中的 `T` 是值參數還是型態參數？
2. 為什麼 `ArrayList<int>` 不合法？
3. Generic class 與 generic method 的適用情況有何不同？
4. `Map` 是否為 `Collection` 的 subtype？
5. List、Set、Map 分別適合回答哪一類問題？
6. 為什麼走訪中刪除資料應使用 `Iterator.remove()`？

## 評分規準

| 評分項目 | 完整達成 | 部分達成 | 尚未達成 |
|---|---|---|---|
| Generic 設計 | Type parameter 角色清楚且無 raw type | 可執行但型態設計過度寬鬆 | 依賴 Object 與不安全 cast |
| Generic method | 宣告與推論正確，邊界完整 | 一般案例正確 | Method 簽章或回傳型態錯誤 |
| 集合選擇 | List/Set/Map 符合資料語意 | 結果正確但選擇理由不足 | 使用錯誤集合導致資料遺失 |
| Iterator | 走訪刪除安全且結果正確 | 能刪除但邊界不足 | 發生 ConcurrentModificationException |
| 程式完整性 | 檔名正確、可編譯、輸出可核對 | 少量錯誤但主要功能存在 | 無法編譯或缺少主要功能 |
| GitHub 繳交 | `0820` 連結與檔案完整 | 命名或紀錄有缺漏 | 未 push 或連結錯誤 |

## 參考教材

- [Dev.java：Introducing Generics](https://dev.java/learn/generics/intro/)
- [Dev.java：The Collections Framework](https://dev.java/learn/api/collections-framework/)
- [Dev.java：Getting to Know the Collection Hierarchy](https://dev.java/learn/api/collections-framework/organization/)
- [Java SE 17 ArrayList API](https://docs.oracle.com/en/java/javase/17/docs/api/java.base/java/util/ArrayList.html)
