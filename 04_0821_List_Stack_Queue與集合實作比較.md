# 8/21 教材：List、Stack、Queue 與集合實作比較

## 單元名稱

ArrayList、LinkedList、Deque、自行實作與 Java 內建集合的選擇

## 課程定位

前一單元認識 Generic 與 Collections Framework。本單元不只練習 API，而是比較不同 implementation 的內部概念、主要操作與適用情境。後續 Tree、Heap、Hash Table 與 Graph 都會同時用到「自行實作核心結構」及「使用 Java 內建集合完成應用」兩種能力。

## 學習目標

完成本單元後，應能：

1. 使用 `List` interface 操作 `ArrayList` 與 `LinkedList`。
2. 說明兩種 List implementation 的主要差異。
3. 使用 `Deque` 實作 Stack 的 LIFO 操作。
4. 使用 `Deque` 實作 Queue 的 FIFO 操作。
5. 自行實作固定容量的 Stack，理解 top 與邊界條件。
6. 比較自行實作與內建集合的優缺點。
7. 根據主要操作選擇合適資料結構。
8. 使用多種集合完成工作流程系統。

## 先備知識

- Generic、interface、polymorphism。
- List、Set、Map 與 Iterator 基本概念。
- 陣列、object reference、exception 的基本閱讀能力。
- 建立 `0821` 資料夾，所有範例依標示檔名存放。

## 問題情境

購物車需要依索引顯示商品，客服案件需要依到達順序處理，編輯器復原需要先取回最近操作，會員編號則不能重複。這些資料都可以硬塞進 `ArrayList`，但操作語意會變得不清楚，部分操作也不符合結構的主要優勢。

選擇資料結構時，應先列出最常發生的操作，再判斷需要順序、索引、去重、key 查詢、LIFO 或 FIFO。資料結構不是由資料名稱決定，而是由操作需求決定。

## 核心概念

### 概念 1：用 List interface 隔離 implementation

#### 概念說明

`List<E>` 定義有順序、允許重複、可依 index 操作的集合行為。`ArrayList<E>` 與 `LinkedList<E>` 都實作 `List<E>`，因此可以使用相同的 reference type：

```java
List<String> names = new ArrayList<>();
List<String> backup = new LinkedList<>();
```

呼叫端依賴 `List` 行為，implementation 可以依主要操作替換。這不代表兩者效能完全相同，而是公開操作契約相同。

#### 實際應用

- Service method 接收 `List<Order>`，不限制呼叫端 implementation。
- 測試時替換不同 List 實作比較結果。
- 應用程式先依介面設計，再根據量測選實作。

#### 資料變化

同一段 `add()`、`get()`、`remove()` 程式可以作用在兩種 List，但內部移動資料的方式不同。

#### 何時適合使用

變數、參數與回傳值只需要 List 行為時，優先宣告為 `List<E>`。確實需要某 implementation 特有 method 時才使用具體型態。

#### 範例程式

檔名：`ListPolymorphismDemo.java`

```java
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class ListPolymorphismDemo {
    static void fillAndPrint(String label, List<String> list) {
        list.add("Tree");
        list.add("Heap");
        list.add("Graph");
        list.add(1, "List");

        System.out.println(label + "：" + list);
        System.out.println("index 2：" + list.get(2));
    }

    public static void main(String[] args) {
        List<String> arrayBased = new ArrayList<>();
        List<String> linked = new LinkedList<>();

        fillAndPrint("ArrayList", arrayBased);
        fillAndPrint("LinkedList", linked);
    }
}
```

執行結果：

```text
ArrayList：[Tree, List, Heap, Graph]
index 2：Heap
LinkedList：[Tree, List, Heap, Graph]
index 2：Heap
```

#### 執行重點

兩次呼叫使用同一個 method。結果相同不代表內部結構與成本相同。

---

### 概念 2：ArrayList 與 LinkedList 的內部概念

#### 概念說明

`ArrayList` 使用可調整容量的 internal array。依 index 讀取可直接定位，通常適合頻繁隨機存取；在中間插入或刪除時，後方 reference 需要移動。

`LinkedList` 使用 doubly linked nodes。已取得節點位置後，調整連結可以快速插入或刪除；但依 index 取得第 n 筆時，需要從前端或後端逐步走訪。

不要把理論複雜度直接當成所有程式的實際速度。`ArrayList` 具有良好的記憶體區域性，常數成本也較低；應根據實際工作量測。

#### 實際應用

- 頻繁 `get(index)`、主要在尾端新增：通常先考慮 ArrayList。
- 頻繁操作兩端：可以使用 LinkedList，但若只需要 queue/stack，通常直接使用 ArrayDeque。
- 已經有 iterator 且需要就地移除：兩者都可透過 iterator 操作。

#### 資料變化

ArrayList 在 index 1 插入 X：

```text
[A, B, C] -> 移動 B、C -> [A, X, B, C]
```

LinkedList 插入 X：

```text
A <-> B <-> C
A <-> X <-> B <-> C
```

#### 何時適合使用

一般 List 需求可先選 ArrayList。只有操作模式明確受益於 linked structure，且量測確認值得時再選 LinkedList。

#### 範例程式

檔名：`ListOperationTrace.java`

```java
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class ListOperationTrace {
    static void trace(List<String> data) {
        data.add("A");
        data.add("B");
        data.add("C");
        System.out.println("尾端新增：" + data);

        data.add(1, "X");
        System.out.println("index 1 插入：" + data);

        data.remove(2);
        System.out.println("index 2 刪除：" + data);

        data.set(1, "Y");
        System.out.println("index 1 修改：" + data);
    }

    public static void main(String[] args) {
        System.out.println("ArrayList");
        trace(new ArrayList<>());

        System.out.println("LinkedList");
        trace(new LinkedList<>());
    }
}
```

執行結果：

```text
ArrayList
尾端新增：[A, B, C]
index 1 插入：[A, X, B, C]
index 2 刪除：[A, X, C]
index 1 修改：[A, Y, C]
LinkedList
尾端新增：[A, B, C]
index 1 插入：[A, X, B, C]
index 2 刪除：[A, X, C]
index 1 修改：[A, Y, C]
```

#### 執行重點

輸出只顯示外部結果。請同時畫出 internal array 移動與 node link 調整，不能只記 API。

---

### 概念 3：Deque 表示雙端操作

#### 概念說明

`Deque<E>` 是 double-ended queue，可以在前端與後端新增、移除及查看資料。常用的兩組方法：

| 操作 | 失敗時丟出例外 | 失敗時回傳特殊值 |
|---|---|---|
| 前端新增 | `addFirst()` | `offerFirst()` |
| 後端新增 | `addLast()` | `offerLast()` |
| 前端移除 | `removeFirst()` | `pollFirst()` |
| 後端移除 | `removeLast()` | `pollLast()` |
| 前端查看 | `getFirst()` | `peekFirst()` |
| 後端查看 | `getLast()` | `peekLast()` |

`ArrayDeque` 是一般 stack 與 queue 的常用 implementation，不允許 `null`，也不是 thread-safe。

#### 實際應用

- 同一結構可由前後兩端處理待辦資料。
- 滑動視窗保留最近資料。
- Stack 與 Queue 的標準實作基礎。

#### 資料變化

```text
[]
offerLast("B")  -> [B]
offerFirst("A") -> [A, B]
offerLast("C")  -> [A, B, C]
pollFirst()      -> [B, C]
```

#### 何時適合使用

操作集中在集合兩端時使用 Deque。需要任意 index 存取時應選 List。

#### 範例程式

檔名：`DequeEndsDemo.java`

```java
import java.util.ArrayDeque;
import java.util.Deque;

public class DequeEndsDemo {
    public static void main(String[] args) {
        Deque<String> tasks = new ArrayDeque<>();

        tasks.offerLast("Normal-1");
        tasks.offerLast("Normal-2");
        tasks.offerFirst("Urgent");

        System.out.println("目前：" + tasks);
        System.out.println("前端：" + tasks.peekFirst());
        System.out.println("後端：" + tasks.peekLast());
        System.out.println("處理：" + tasks.pollFirst());
        System.out.println("剩餘：" + tasks);
    }
}
```

執行結果：

```text
目前：[Urgent, Normal-1, Normal-2]
前端：Urgent
後端：Normal-2
處理：Urgent
剩餘：[Normal-1, Normal-2]
```

#### 執行重點

使用帶有 `First`、`Last` 的 method 可以清楚表達操作方向。

---

### 概念 4：使用 Deque 實作 Stack

#### 概念說明

Stack 遵守 LIFO：last in, first out。Java 可使用 `Deque` 的 stack method：

- `push(value)`：加入頂端。
- `pop()`：移除並回傳頂端，空 stack 會丟出例外。
- `peek()`：查看頂端，空 stack 回傳 `null`。

若希望空結構不丟出例外，也可以一致使用 `offerFirst()`、`pollFirst()`、`peekFirst()`。

#### 實際應用

- Undo history。
- 括號配對。
- DFS iterative traversal。
- Method call stack 的概念模型。

#### 資料變化

```text
push(A) -> [A]
push(B) -> [B, A]
push(C) -> [C, B, A]
pop()   -> C，剩下 [B, A]
```

#### 何時適合使用

下一筆一定是最近加入、尚未處理的資料時使用 Stack。若最早加入者應先處理，應使用 Queue。

#### 範例程式

檔名：`UndoStackDemo.java`

```java
import java.util.ArrayDeque;
import java.util.Deque;

public class UndoStackDemo {
    static String undo(Deque<String> history) {
        String action = history.pollFirst();
        return action == null ? "EMPTY" : action;
    }

    public static void main(String[] args) {
        Deque<String> history = new ArrayDeque<>();

        history.push("Open file");
        history.push("Type title");
        history.push("Delete line");

        System.out.println("最近操作：" + history.peek());
        System.out.println("復原：" + undo(history));
        System.out.println("復原：" + undo(history));
        System.out.println("剩餘：" + history);
    }
}
```

執行結果：

```text
最近操作：Delete line
復原：Delete line
復原：Type title
剩餘：[Open file]
```

#### 執行重點

所有 stack 操作都在同一端進行。混用前端與後端會破壞 LIFO。

---

### 概念 5：使用 Deque 實作 Queue

#### 概念說明

Queue 遵守 FIFO：first in, first out。使用 Deque 時：

- `offerLast(value)`：加入隊尾。
- `pollFirst()`：從隊首取出。
- `peekFirst()`：查看下一筆。

也可以使用 Queue interface 的 `offer()`、`poll()`、`peek()`，但在需要明確說明兩端時，帶方向的 method 更容易閱讀。

#### 實際應用

- 客服等候隊列。
- BFS traversal。
- 工作排程。
- 訊息處理。

#### 資料變化

```text
offer(A) -> [A]
offer(B) -> [A, B]
offer(C) -> [A, B, C]
poll()   -> A，剩下 [B, C]
```

#### 何時適合使用

最早到達的資料應先處理時使用 Queue。若有優先順序，不應只靠一般 FIFO Queue，後續會使用 PriorityQueue。

#### 範例程式

檔名：`ServiceQueueDemo.java`

```java
import java.util.ArrayDeque;
import java.util.Deque;

public class ServiceQueueDemo {
    static String serveNext(Deque<String> waiting) {
        String customer = waiting.pollFirst();
        return customer == null ? "EMPTY" : customer;
    }

    public static void main(String[] args) {
        Deque<String> waiting = new ArrayDeque<>();

        waiting.offerLast("A101 Amy");
        waiting.offerLast("A102 Ben");
        waiting.offerLast("A103 Cara");

        System.out.println("下一位：" + waiting.peekFirst());
        System.out.println("服務：" + serveNext(waiting));
        System.out.println("服務：" + serveNext(waiting));
        System.out.println("剩餘：" + waiting);
    }
}
```

執行結果：

```text
下一位：A101 Amy
服務：A101 Amy
服務：A102 Ben
剩餘：[A103 Cara]
```

#### 執行重點

加入與取出在不同端，才能維持 FIFO。

---

### 概念 6：自行實作固定容量 Stack

#### 概念說明

使用 `ArrayDeque` 可以快速完成應用，但自行實作能看見資料結構的核心狀態。固定容量 array stack 需要：

- `data[]`：保存元素。
- `size`：目前元素數量，也可作為下一個寫入 index。
- `push()`：先檢查是否已滿，再寫入 `data[size]` 並增加 size。
- `pop()`：先檢查是否為空，再減少 size 並讀取原頂端。
- `peek()`：讀取 `data[size - 1]`，不改變 size。

#### 實際應用

- 理解 overflow 與 underflow。
- 觀察 top/size 如何控制有效資料範圍。
- 後續實作 Heap 時延續「有效長度與底層陣列」概念。

#### 資料變化

容量 3：

```text
data=[null, null, null], size=0
push(A) -> data=[A, null, null], size=1
push(B) -> data=[A, B, null], size=2
pop()   -> 回傳 B, size=1
```

#### 何時適合使用

學習核心原理、有固定容量需求或需要控制記憶體配置時自行實作。一般應用優先使用經測試的標準函式庫。

#### 範例程式

檔名：`CustomStringStackDemo.java`

```java
class StringStack {
    private String[] data;
    private int size;

    StringStack(int capacity) {
        data = new String[Math.max(1, capacity)];
    }

    boolean push(String value) {
        if (value == null || size == data.length) {
            return false;
        }
        data[size] = value;
        size++;
        return true;
    }

    String pop() {
        if (size == 0) {
            return null;
        }
        size--;
        String value = data[size];
        data[size] = null;
        return value;
    }

    String peek() {
        return size == 0 ? null : data[size - 1];
    }

    int size() {
        return size;
    }
}

public class CustomStringStackDemo {
    public static void main(String[] args) {
        StringStack stack = new StringStack(2);

        System.out.println("push A：" + stack.push("A"));
        System.out.println("push B：" + stack.push("B"));
        System.out.println("push C：" + stack.push("C"));
        System.out.println("peek：" + stack.peek());
        System.out.println("pop：" + stack.pop());
        System.out.println("size：" + stack.size());
    }
}
```

執行結果：

```text
push A：true
push B：true
push C：false
peek：B
pop：B
size：1
```

#### 執行重點

`pop()` 後將已離開有效範圍的 reference 設為 `null`，避免不必要地保留物件 reference。

---

### 概念 7：自行實作與內建集合的比較

#### 概念說明

自行實作與使用內建集合不是互斥選項，而是目的不同：

| 面向 | 自行實作 | Java 內建集合 |
|---|---|---|
| 主要目的 | 理解結構、控制特殊規則 | 快速建立可靠應用 |
| 邊界處理 | 必須自行完成 | API 已定義 |
| Generic | 需要自行設計 | 多數已支援 |
| 測試成本 | 高 | 標準實作已廣泛測試 |
| 客製化 | 可完全控制 | 依公開 API 使用 |

課程會要求自行實作核心資料結構，但整合應用時仍會使用內建集合。例如自行實作 Graph adjacency list 時，可使用 `ArrayList` 保存每個頂點的鄰居。

#### 實際應用

- 面試或演算法課程：自行實作核心操作。
- 一般系統：優先使用標準集合，將時間投入業務規則。
- 特殊容量或記憶體限制：評估客製實作。

#### 資料變化

```text
push 10, push 20
custom array stack : data=[10, 20], size=2
ArrayDeque stack   : [20, 10]
pop 後兩者都回傳 20
```

內部表示不同，但只要對外契約相同，push、pop 與 LIFO 結果應該一致。

#### 何時適合使用

若目標是學習內部原理或標準集合無法表達特殊約束，才自行實作。不要在正式系統中無理由重寫整個 Collections Framework。

#### 範例程式

檔名：`StackImplementationComparison.java`

```java
import java.util.ArrayDeque;
import java.util.Deque;

class FixedIntStack {
    private int[] data;
    private int size;

    FixedIntStack(int capacity) {
        data = new int[Math.max(1, capacity)];
    }

    boolean push(int value) {
        if (size == data.length) {
            return false;
        }
        data[size++] = value;
        return true;
    }

    Integer pop() {
        return size == 0 ? null : data[--size];
    }
}

public class StackImplementationComparison {
    public static void main(String[] args) {
        FixedIntStack custom = new FixedIntStack(2);
        custom.push(10);
        custom.push(20);

        Deque<Integer> builtIn = new ArrayDeque<>();
        builtIn.push(10);
        builtIn.push(20);

        System.out.println("自訂 pop：" + custom.pop());
        System.out.println("內建 pop：" + builtIn.pop());
    }
}
```

執行結果：

```text
自訂 pop：20
內建 pop：20
```

#### 執行重點

外部結果相同，但 custom stack 必須自行負責容量與空結構；`ArrayDeque` 可動態成長並已提供完整 API。

---

### 概念 8：綜合應用，多種集合組成工作流程

#### 概念說明

實際系統通常不只使用一個集合。應依每個責任分別選擇：

- `Map<String, Task>`：以 id 快速查詢全部任務。
- `Deque<Task>` 作為 Queue：保存等待處理順序。
- `Deque<Task>` 作為 Stack：保存完成歷程，支援 undo。
- `Set<String>`：保存不重複標籤。

同一個 Task object 可以被不同集合 reference 共用。改變任務狀態時，各集合指向的仍是同一物件。

#### 實際應用

- 工單系統：Map 查詢、Queue 排程、Stack 復原。
- 瀏覽器：Map 保存頁面、Stack 保存上一頁與下一頁。
- Graph traversal：Map 保存節點、Queue/Stack 控制走訪順序、Set 保存 visited。

#### 資料變化

| 操作 | Map 中狀態 | Waiting Queue | Completed Stack |
|---|---|---|---|
| 新增 T101、T102 | 兩筆 incomplete | `[T101, T102]` | `[]` |
| 處理 T101 | T101 completed | `[T102]` | `[T101]` |
| undo T101 | T101 reopened | `[T101, T102]` | `[]` |

#### 何時適合使用

需求同時包含不同操作語意時，讓每個集合負責自己擅長的工作。不要為了只維護一份集合而犧牲清楚的操作模型。

#### 範例程式

檔名：`WorkflowCollectionsDemo.java`

```java
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.LinkedHashMap;
import java.util.Map;

class WorkTask {
    private String id;
    private String title;
    private boolean completed;

    WorkTask(String id, String title) {
        this.id = id;
        this.title = title;
    }

    String getId() {
        return id;
    }

    void complete() {
        completed = true;
    }

    void reopen() {
        completed = false;
    }

    @Override
    public String toString() {
        return id + " " + title + " completed=" + completed;
    }
}

public class WorkflowCollectionsDemo {
    public static void main(String[] args) {
        Map<String, WorkTask> tasksById = new LinkedHashMap<>();
        Deque<WorkTask> waiting = new ArrayDeque<>();
        Deque<WorkTask> completedHistory = new ArrayDeque<>();

        WorkTask first = new WorkTask("T101", "Backup");
        WorkTask second = new WorkTask("T102", "Update");

        tasksById.put(first.getId(), first);
        tasksById.put(second.getId(), second);
        waiting.offerLast(first);
        waiting.offerLast(second);

        WorkTask processed = waiting.pollFirst();
        processed.complete();
        completedHistory.push(processed);

        WorkTask undone = completedHistory.pollFirst();
        undone.reopen();
        waiting.offerFirst(undone);

        System.out.println("查詢：" + tasksById.get("T101"));
        System.out.println("下一筆：" + waiting.peekFirst());
        System.out.println("等待數：" + waiting.size());
    }
}
```

執行結果：

```text
查詢：T101 Backup completed=false
下一筆：T101 Backup completed=false
等待數：2
```

#### 執行重點

Map、waiting queue 與 completed stack 保存的是 object reference。Undo 改變物件狀態後，Map 查到的狀態也同步更新。

---

### 概念 9：自行實作 dynamic array 與擴容

#### 概念說明

ArrayList 對外提供可變長度的 List，但內部核心仍然可以使用 array 保存資料。因為 array 長度建立後無法改變，當容量不足時必須：

1. 建立更大的新陣列。
2. 將舊陣列的有效元素複製到新陣列。
3. 讓內部 reference 改指向新陣列。

`size` 是已使用元素數，`capacity` 是內部陣列長度。`size <= capacity` 必須永遠成立。

#### 實際應用

- 理解 ArrayList 尾端 add 平均很快，但偶爾需要搬移整批資料。
- 建立小型快取、記錄器或可變長度 buffer。
- 比較尾端新增與中間插入的搬移成本。

#### 資料變化

```text
capacity=2, size=0: [_, _]
add 10             : [10, _], size=1
add 20             : [10, 20], size=2
add 30 -> resize 4 : [10, 20, 30, _], size=3
remove index 1     : [10, 30, _, _], size=2
```

#### 設計判斷

擴容如果每次只增加 1，連續 add 會反覆複製大量資料。成倍擴張可以降低擴容次數。但容量放大也會保留尚未使用的空間，這是時間與空間的取捨。

#### 範例程式

檔名：`CustomDynamicArrayDemo.java`

```java
import java.util.Arrays;

class IntDynamicArray {
    private int[] data;
    private int size;

    IntDynamicArray(int initialCapacity) {
        data = new int[Math.max(1, initialCapacity)];
    }

    void add(int value) {
        ensureCapacity();
        data[size] = value;
        size++;
    }

    int get(int index) {
        checkIndex(index);
        return data[index];
    }

    int remove(int index) {
        checkIndex(index);
        int removed = data[index];
        for (int i = index; i < size - 1; i++) {
            data[i] = data[i + 1];
        }
        size--;
        data[size] = 0;
        return removed;
    }

    int size() {
        return size;
    }

    int capacity() {
        return data.length;
    }

    private void ensureCapacity() {
        if (size == data.length) {
            data = Arrays.copyOf(data, data.length * 2);
            System.out.println("resize -> " + data.length);
        }
    }

    private void checkIndex(int index) {
        if (index < 0 || index >= size) {
            throw new IndexOutOfBoundsException("index=" + index);
        }
    }

    @Override
    public String toString() {
        return Arrays.toString(Arrays.copyOf(data, size));
    }
}

public class CustomDynamicArrayDemo {
    public static void main(String[] args) {
        IntDynamicArray values = new IntDynamicArray(2);
        values.add(10);
        values.add(20);
        values.add(30);

        System.out.println(values);
        System.out.println("removed=" + values.remove(1));
        System.out.println(values);
        System.out.println("size=" + values.size()
                + ", capacity=" + values.capacity());
    }
}
```

執行結果：

```text
resize -> 4
[10, 20, 30]
removed=20
[10, 30]
size=2, capacity=4
```

#### 實作變化

新增 `add(int index, int value)`，允許 index 範圍為 0 至 size。插入前要先擴容，再將 index 以後的元素由後往前搬移。

---

### 概念 10：自行實作 singly linked list

#### 概念說明

Singly linked list 由多個 node 組成。每個 node 保存一筆 element 與下一個 node 的 reference。List 本身保存 `head`，即第一個 node。

與 array 不同，linked list 不要求 element 在連續 index 中。在已知 node 位置時，連接新 node 不需要搬移後面所有元素；但根據 index 找資料時，必須從 head 逐節往後走。

#### 實際應用

- 需要頻繁調整連結關係的自訂資料結構。
- Graph adjacency list 與 hash table chaining 的概念基礎。
- 理解 Java `LinkedList` 為什麼不適合大量使用 `get(index)`。

#### 資料變化

```text
addLast A: head -> [A|null]
addLast B: head -> [A|next] -> [B|null]
addFirst X: head -> [X|next] -> [A|next] -> [B|null]
remove A: head -> [X|next] -> [B|null]
```

#### 設計判斷

自行實作 linked list 是為了理解 node 與 reference 變化。在一般應用程式中，應優先使用已測試的 Java collection，除非需要特殊節點結構或題目明確要求實作。

#### 範例程式

檔名：`SinglyLinkedListDemo.java`

```java
class ListNode<T> {
    T value;
    ListNode<T> next;

    ListNode(T value) {
        this.value = value;
    }
}

class SimpleLinkedList<T> {
    private ListNode<T> head;
    private int size;

    void addFirst(T value) {
        ListNode<T> node = new ListNode<>(value);
        node.next = head;
        head = node;
        size++;
    }

    void addLast(T value) {
        ListNode<T> node = new ListNode<>(value);
        if (head == null) {
            head = node;
        } else {
            ListNode<T> current = head;
            while (current.next != null) {
                current = current.next;
            }
            current.next = node;
        }
        size++;
    }

    T get(int index) {
        if (index < 0 || index >= size) {
            throw new IndexOutOfBoundsException("index=" + index);
        }
        ListNode<T> current = head;
        for (int i = 0; i < index; i++) {
            current = current.next;
        }
        return current.value;
    }

    boolean remove(T target) {
        if (head == null) {
            return false;
        }
        if (java.util.Objects.equals(head.value, target)) {
            head = head.next;
            size--;
            return true;
        }
        ListNode<T> current = head;
        while (current.next != null) {
            if (java.util.Objects.equals(current.next.value, target)) {
                current.next = current.next.next;
                size--;
                return true;
            }
            current = current.next;
        }
        return false;
    }

    int size() {
        return size;
    }

    @Override
    public String toString() {
        StringBuilder result = new StringBuilder("[");
        ListNode<T> current = head;
        while (current != null) {
            result.append(current.value);
            current = current.next;
            if (current != null) {
                result.append(", ");
            }
        }
        return result.append("]").toString();
    }
}

public class SinglyLinkedListDemo {
    public static void main(String[] args) {
        SimpleLinkedList<String> list = new SimpleLinkedList<>();
        list.addLast("A");
        list.addLast("B");
        list.addFirst("X");

        System.out.println(list);
        System.out.println("index 1=" + list.get(1));
        System.out.println("remove A=" + list.remove("A"));
        System.out.println(list + " size=" + list.size());
    }
}
```

執行結果：

```text
[X, A, B]
index 1=A
remove A=true
[X, B] size=2
```

#### 實作變化

新增 `T removeFirst()`。空 list 回傳 `null`，非空 list 要回傳原本 head 的 value、移動 head 並將 size 減 1。

---

### 概念 11：Circular array queue

#### 概念說明

如果 array queue 每次 dequeue 都將所有元素往前搬，操作成本會隨資料量增加。Circular queue 不搬移元素，而是使用 `front`、`rear` 與 `size` 記錄有效區域。

下一個 index 使用 `(index + 1) % data.length` 計算。當 index 到達陣列最後一格，下一個位置會回到 0，將已經 dequeue 留下的空間重新使用。

#### 實際應用

- 固定容量的輸入 buffer、裝置資料與任務佇列。
- 需要長時間重複 enqueue/dequeue，但不希望搬移元素。
- 後續理解 BFS queue 與有界 buffer。

#### 資料變化

| 操作 | Array | front | rear | size |
|---|---|---:|---:|---:|
| enqueue 10 | `[10, _, _]` | 0 | 1 | 1 |
| enqueue 20 | `[10, 20, _]` | 0 | 2 | 2 |
| dequeue | `[_, 20, _]` | 1 | 2 | 1 |
| enqueue 30 | `[_, 20, 30]` | 1 | 0 | 2 |
| enqueue 40 | `[40, 20, 30]` | 1 | 1 | 3 |

#### 設計判斷

`front == rear` 可能表示空，也可能表示滿。這份實作另外保存 `size`，所以 `size == 0` 是空、`size == data.length` 是滿，不需要浪費一格來區分狀態。

#### 範例程式

檔名：`CircularArrayQueueDemo.java`

```java
import java.util.Arrays;

class CircularIntQueue {
    private final int[] data;
    private int front;
    private int rear;
    private int size;

    CircularIntQueue(int capacity) {
        data = new int[Math.max(1, capacity)];
    }

    boolean enqueue(int value) {
        if (isFull()) {
            return false;
        }
        data[rear] = value;
        rear = (rear + 1) % data.length;
        size++;
        return true;
    }

    Integer dequeue() {
        if (isEmpty()) {
            return null;
        }
        int value = data[front];
        data[front] = 0;
        front = (front + 1) % data.length;
        size--;
        return value;
    }

    Integer peek() {
        return isEmpty() ? null : data[front];
    }

    boolean isEmpty() {
        return size == 0;
    }

    boolean isFull() {
        return size == data.length;
    }

    void printState() {
        System.out.println(Arrays.toString(data)
                + " front=" + front + " rear=" + rear + " size=" + size);
    }
}

public class CircularArrayQueueDemo {
    public static void main(String[] args) {
        CircularIntQueue queue = new CircularIntQueue(3);
        queue.enqueue(10);
        queue.enqueue(20);
        queue.printState();

        System.out.println("dequeue=" + queue.dequeue());
        queue.enqueue(30);
        queue.enqueue(40);
        queue.printState();

        System.out.println("full=" + queue.isFull());
        System.out.println("enqueue 50=" + queue.enqueue(50));
        System.out.println("peek=" + queue.peek());
    }
}
```

執行結果：

```text
[10, 20, 0] front=0 rear=2 size=2
dequeue=10
[40, 20, 30] front=1 rear=1 size=3
full=true
enqueue 50=false
peek=20
```

#### 實作變化

新增 `clear()`，將所有格子清為 0，並將 front、rear、size 恢復為 0。清空後 `dequeue()` 必須回傳 `null`。

---

### 概念 12：Stack 應用，括號配對

#### 概念說明

括號配對不只是統計左括號與右括號的數量。順序也必須正確，例如 `([)]` 數量相同，但嵌套順序錯誤。

讀到左括號時 push 進 stack；讀到右括號時，必須和 stack top 的最近左括號配對。全部字元處理後，stack 也必須為空。

#### 實際應用

- Compiler 與 IDE 檢查程式碼 delimiter。
- 驗證數學式、設定檔與簡化標記語法。
- 後續的 expression evaluation 與 syntax parsing。

#### 資料變化

```text
expression = {[()]}
{ -> [{]
[ -> [{, []
( -> [{, [, (]
) -> [{, []
] -> [{]
} -> []
```

#### 設計判斷

Stack 保存的是「尚未配對的左括號」。每次右括號只能和最近的左括號配對，正好對應 LIFO。

#### 範例程式

檔名：`BracketMatchingDemo.java`

```java
import java.util.ArrayDeque;
import java.util.Deque;

public class BracketMatchingDemo {
    static boolean isBalanced(String expression) {
        if (expression == null) {
            return false;
        }

        Deque<Character> stack = new ArrayDeque<>();
        for (char symbol : expression.toCharArray()) {
            if (symbol == '(' || symbol == '[' || symbol == '{') {
                stack.push(symbol);
            } else if (symbol == ')' || symbol == ']' || symbol == '}') {
                if (stack.isEmpty() || !matches(stack.pop(), symbol)) {
                    return false;
                }
            }
        }
        return stack.isEmpty();
    }

    static boolean matches(char open, char close) {
        return (open == '(' && close == ')')
                || (open == '[' && close == ']')
                || (open == '{' && close == '}');
    }

    public static void main(String[] args) {
        String[] expressions = {
            "{[()]}", "([)]", "(()", "a + (b * c)", ""
        };

        for (String expression : expressions) {
            System.out.println(expression + " -> " + isBalanced(expression));
        }
    }
}
```

執行結果：

```text
{[()]} -> true
([)] -> false
(() -> false
a + (b * c) -> true
 -> true
```

#### 實作變化

新增 `firstErrorIndex(String expression)`，第一個無法配對的右括號回傳其 index；全部正確回傳 `-1`。如果最後還留有左括號，回傳 expression length。

## 程式執行追蹤

### 追蹤一：Dynamic array 擴容與刪除

| 操作 | size | capacity | 有效資料 | 搬移動作 |
|---|---:|---:|---|---|
| 初始 | 0 | 2 | `[]` | 無 |
| add 10 | 1 | 2 | `[10]` | 無 |
| add 20 | 2 | 2 | `[10, 20]` | 無 |
| add 30 | 3 | 4 | `[10, 20, 30]` | 複製 2 個元素 |
| remove(1) | 2 | 4 | `[10, 30]` | 30 往前搬一格 |

### 追蹤二：Circular queue 繞回陣列開頭

Rear 使用 `(rear + 1) % capacity` 移動。容量為 3 時，index 變化為 `0 -> 1 -> 2 -> 0`。Dequeue 只移動 front，不搬移後面元素。

## 除錯練習

### 除錯練習一：Dynamic array off-by-one

```java
if (index < 0 || index > size) {
    throw new IndexOutOfBoundsException();
}
return data[index];
```

`get(index)` 的有效範圍是 `0 <= index < size`，因此條件必須是 `index >= size`。只有插入位置才可以允許 `index == size`。

### 除錯練習二：Circular queue 沒有區分空與滿

如果只保存 front 與 rear，並同時以 `front == rear` 判斷空和滿，會無法區分兩種狀態。可以另存 `size`，或永遠留一格不使用。本教材的實作選擇保存 `size`。

## 課堂實作題

### 課堂實作題一：List Implementation 比較

指定檔名：`ListImplementationLab.java`

撰寫只接收 `List<Integer>` 的 method，完成尾端新增、指定位置插入、搜尋、刪除與總和。分別傳入 `ArrayList`、`LinkedList`，確認功能結果一致，再以文字說明兩者可能的內部成本差異。

### 課堂實作題二：瀏覽器返回功能

指定檔名：`BrowserBackStack.java`

使用 `Deque<String>` 保存瀏覽歷程，完成 visit、back、current。空 stack 時不得丟出例外，連續測試至少五個操作。

### 課堂實作題三：櫃台等候 Queue

指定檔名：`CounterWaitingQueue.java`

使用 `Deque<Customer>` 管理一般顧客 FIFO 隊列，完成加入、查看下一位、服務下一位、顯示等候數與空隊列處理。

### 課堂實作題四：固定容量 Generic Stack

指定檔名：`GenericArrayStackDemo.java`

將本單元的 `StringStack` 改為 `ArrayStack<T>`，提供 push、pop、peek、size、isEmpty、isFull。使用 `ArrayStack<String>` 與 `ArrayStack<Integer>` 測試。

完成標準：不得使用 Java Stack、Deque 或 List 代替底層 array。

### 課堂實作題五：Dynamic array 插入與刪除

指定檔名：`DynamicArrayPractice.java`

建立 generic `DynamicArray<T>`，底層使用 `Object[]`，完成：

```java
void add(T value)
void add(int index, T value)
T get(int index)
T set(int index, T value)
T remove(int index)
int size()
int capacity()
```

- 容量滿時擴充為兩倍。
- 移除後最後一個無效格要設為 `null`。
- 分別使用 String 與 Integer 測試。
- 測試 index `-1`、`size` 與空結構刪除。

### 課堂實作題六：Circular queue 狀態追蹤

指定檔名：`CircularQueuePractice.java`

以容量 4 建立 `CircularQueue<String>`，連續執行以下操作：

```text
enqueue A, enqueue B, enqueue C,
dequeue, dequeue,
enqueue D, enqueue E, enqueue F,
dequeue, enqueue G
```

每次操作後輸出內部 array、front、rear、size。最後要依 FIFO 順序取出所有元素。

完成標準：不可在 dequeue 時搬移全部元素，必須使用 modulo 循環 index。

## 課後作業

### 課後作業一：文字編輯 Undo/Redo

指定檔名：`TextEditorHistory.java`

使用兩個 `Deque<String>` 分別作為 undo 與 redo stack。新增操作後清空 redo；undo 將資料移到 redo；redo 將資料移回 undo。處理空 stack 並輸出每一步狀態。

### 課後作業二：診所掛號系統

指定檔名：`ClinicQueueSystem.java`

建立 `Patient` 與診所 Queue，完成一般掛號、取消指定病歷號、叫號、查看下一位與當日完成清單。一般叫號必須維持 FIFO。

### 課後作業三：物流工作流程

指定檔名：`DeliveryWorkflowSystem.java`

使用 Map 依配送編號查詢、Queue 保存等待配送、Stack 保存已完成歷程，完成新增、處理、undo、查詢與統計。重複 id 不得加入。

### 課後作業四：集合選擇報告與實作

指定檔名：`CollectionChoiceReport.java`

針對下列需求選擇結構並實作主要操作：

1. 保留搜尋紀錄且允許重複。
2. 保存不重複會員編號。
3. 以學號查詢成績。
4. 依到達順序處理列印工作。
5. 復原最近操作。

程式輸出每個需求選擇的 interface、implementation 與操作結果。

### 課後作業五：單向鏈結清單

指定檔名：`LinkedTaskListSystem.java`

自行實作 `TaskNode` 與 `TaskLinkedList`，不可使用 Java List 作為底層結構。提供：

- `addFirst(Task task)`、`addLast(Task task)`。
- `findById(String id)`。
- `removeById(String id)`。
- `insertAfter(String existingId, Task task)`。
- `size()` 與 `printAll()`。

重複 id 不得加入。必須測試空 list、刪除 head、刪除 middle、刪除 tail 與找不到 id。

### 課後作業六：服務中心排隊與取消

指定檔名：`ServiceCenterWorkflow.java`

使用：

- `Map<String, ServiceTicket>` 依 ticket id 查詢。
- `Deque<ServiceTicket>` 作為等待 Queue。
- `Deque<ServiceTicket>` 作為完成歷程 Stack。
- `Set<String>` 防止重複 id。

完成 `createTicket`、`processNext`、`cancelWaiting`、`undoLastCompletion`、`findById` 與 `printSummary`。

取消只能作用於尚未處理的 ticket；undo 必須將最後完成的 ticket 放回 waiting queue 前端。至少測試重複 id、空 Queue、取消不存在 id 與連續兩次 undo。

## 常見錯誤與診斷

| 問題 | 常見原因 | 修正方式 |
|---|---|---|
| Queue 變成 LIFO | 加入與取出都在同一端 | Queue 使用 `offerLast()` + `pollFirst()` |
| Stack 變成 FIFO | Push 與 pop 使用不同端 | Stack 所有操作固定在同一端 |
| 空集合丟出例外 | 使用 `remove()`、`pop()` 但未檢查 | 改用 `poll()` 或先檢查 `isEmpty()` |
| `IndexOutOfBoundsException` | List index 小於 0 或大於等於 size | 操作前確認 index 範圍 |
| 自訂 Stack overflow | Push 前沒有檢查容量 | `size == data.length` 時拒絕加入 |
| 自訂 Stack underflow | Pop 前沒有檢查 size | 空 stack 回傳明確結果或丟出定義好的例外 |
| LinkedList 依 index 大量走訪很慢 | 每次 get 都需要 traversal | 改用 iterator、for-each 或 ArrayList |
| 同一任務狀態不同步 | 建立了多份 copy 而非共用 reference | 明確決定 object ownership 與 reference sharing |

## 形成性評量

1. `List<String> data = new ArrayList<>();` 的 interface type 與 implementation type 各是什麼？
2. ArrayList 與 LinkedList 在 index 存取上有何差異？
3. Stack 與 Queue 的加入、移除方向分別是什麼？
4. 為什麼一般 stack/queue 建議使用 `ArrayDeque` 而不是舊的 `Stack` class？
5. 自訂 array stack 的 `size` 同時可以代表哪些資訊？
6. 工作流程為什麼可能同時需要 Map、Queue 與 Stack？

## 評分規準

| 評分項目 | 完整達成 | 部分達成 | 尚未達成 |
|---|---|---|---|
| List 選擇 | 能依操作比較 ArrayList/LinkedList | 功能正確但理由不足 | 把兩者視為完全相同 |
| Stack/Queue | LIFO、FIFO 與兩端操作完全正確 | 一般流程正確，空結構不足 | 操作方向錯誤 |
| 自訂實作 | Array、size、overflow、underflow 完整 | 核心可用但邊界不足 | 使用內建集合取代要求實作 |
| 綜合集合 | 每個集合責任清楚且資料一致 | 可執行但責任混雜 | 單一集合勉強處理所有需求 |
| 測試 | 包含空、滿、單筆及連續操作 | 只測一般案例 | 沒有可核對測試 |
| GitHub 繳交 | `0821` 連結、檔案與 commit 完整 | 命名或紀錄有缺漏 | 未 push 或連結錯誤 |

## 參考教材

- [Java SE 17 ArrayList API](https://docs.oracle.com/en/java/javase/17/docs/api/java.base/java/util/ArrayList.html)
- [Java SE 17 LinkedList API](https://docs.oracle.com/en/java/javase/17/docs/api/java.base/java/util/LinkedList.html)
- [Java SE 17 Deque API](https://docs.oracle.com/en/java/javase/17/docs/api/java.base/java/util/Deque.html)
- [Java SE 17 ArrayDeque API](https://docs.oracle.com/en/java/javase/17/docs/api/java.base/java/util/ArrayDeque.html)
- [Dev.java：The Collections Framework](https://dev.java/learn/api/collections-framework/)
