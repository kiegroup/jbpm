
# Recursive vs. queue-based execution semantics

## Introduction

The idea is to split a recursive stack of methods into a groups of methods, *queue actions*, and 
execute these *actions* or groups of methods fully before calling the next group of methods. 

This is instead of the recursive situation, in which the stack of methods grows until 
the process instance has ended or otherwise hit a save point. 

In the code, each *action* is just a POJO that contains 

1. A field referencing a part of the process instance (for example, a `NodeInstance`, 
`EventListener` or event a `WorkItemManager` implementation) 
2. 0 or more fields referencing the arguments used in the method call. Depending on the method called, the *action* might also contain the arguments used in the method call.
3. Lastly, the *action* implements the `org.jbpm.workflow.instance.impl.queue.ProcessInstanceAction` interface, 
which contains the `void trigger()` method. A `ProcessImplementationAction` implementation will then also 

During queue-based execution, this queue of actions is **not** fully filled before-hand, 
but will often only contain 1 or 2 actions. Executing each action often leads to more actions being added to the queue. 

----

One of the main differences between the approach is that far more "state" is saved by the
queue-based implementation. This is to be expected -- the advantage of a recursive algorithm is that
it contains less state than non-recursive algorithms. 

Of course, the disadvantage of a recursive algorithm is that you need extra logic in order to track 
where you are in the algorithm. For example, a recursive algorithm needs extra logic to check
when to stop or start, while a non-recursive algorithm simply stores this information in a 
variable. 

(Compare a classic for loop that does summation to a recursive algorithm that does the same thing: 
the for loop will have an index (`i = 0; i < total; ++i`) while the recursive algorithm will 
simply pass and then modify a variable in the function until it hits a limit. )

These same pros and cons apply to the jBPM recursive and queue-based execution: the recursive
implementation is "larger" while the queue-based implemenation requires more object creation. 

Unfortunately, we have *both* at the moment (for backwards compatibility), which means that 
the queue-based implemenation not only includes the recursive implementation but also requires 
more object creation. This of course puts the queue-based implementation at a disadvantage with 
regards to performance in some (but not all!) use cases. 

### Recursive ("normal") execution

In other words, the "normal", recursive execution of the process engine looks (basically) like this: 

The process instance is triggered... 
```
RuleFlowProcessInstance(WorkflowProcessInstanceImpl).start(String) 
RuleFlowProcessInstance(ProcessInstanceImpl).start(String) 
RuleFlowProcessInstance.internalStart(String) 
```
...which triggers a `StartNodeInstance`...
```
StartNodeInstance(NodeInstanceImpl).trigger(NodeInstance, String) 
StartNodeInstance.internalTrigger(NodeInstance, String) 
StartNodeInstance.triggerCompleted() 
StartNodeInstance(NodeInstanceImpl).triggerCompleted(String, boolean) 
StartNodeInstance(NodeInstanceImpl).triggerNodeInstance(NodeInstance, String) 
```
...which triggers the next `NodeInstance` (a script task, in this case)...
```
ActionNodeInstance(NodeInstanceImpl).trigger(NodeInstance, String) 
ActionNodeInstance.internalTrigger(NodeInstance, String) 
ActionNodeInstance.triggerCompleted() 
ActionNodeInstance(NodeInstanceImpl).triggerCompleted(String, boolean) 
ActionNodeInstance(NodeInstanceImpl).triggerNodeInstance(NodeInstance, String) 
```
...which triggers the next `NodeInstance` (an `EndNodeInstance`, in this case)...
```
EndNodeInstance(NodeInstanceImpl).trigger(NodeInstance, String) 
EndNodeInstance.internalTrigger(NodeInstance, String) 
```
...which triggers the end of the process instance. 
```
RuleFlowProcessInstance(WorkflowProcessInstanceImpl).setState(int) 
```

After which the stack "retreats", after completing all of the methods called (see below for more detail). 

### Queue-based execution

A large part of the stack can be "split" at the `org.jbpm.workflow.instance.NodeInstance.trigger(NodeInstance from, String type)`
method: in other words, each queue-based action starts with calling that method and ends before the next call of that method (on the subsequent node). 

For example, an execution of the same process above might look like this: 

The process instance is triggered.. 
```
RuleFlowProcessInstance(WorkflowProcessInstanceImpl).start(String)
RuleFlowProcessInstance(ProcessInstanceImpl).start(String)
RuleFlowProcessInstance.internalStart(String)
RuleFlowProcessInstance.executeQueue()
RuleFlowProcessInstance.internalExecuteQueue()
```

..and the first `NodeInstanceTriggerAction` (for the `StartNodeInstance`) is triggered

```
RuleFlowProcessInstance.internalExecuteQueue()
NodeInstanceTriggerAction.trigger()
StartNodeInstance(NodeInstanceImpl).trigger(NodeInstance, String)
StartNodeInstance.internalTrigger(NodeInstance, String)
StartNodeInstance.triggerCompleted()
StartNodeInstance(NodeInstanceImpl).triggerCompleted(String, boolean)
StartNodeInstance(NodeInstanceImpl).triggerNodeInstances(Deque<NodeInstanceTriggerAction>)
StartNodeInstance(NodeInstanceImpl).triggerNodeInstance(NodeInstance, String)
StartNodeInstance(NodeInstanceImpl).addNodeInstanceTrigger(NodeInstance, NodeInstance, String)
RuleFlowProcessInstance.addNodeInstanceTriggerAction(NodeInstance, NodeInstance, String)
RuleFlowProcessInstance.addProcessInstanceAction(ProcessInstanceAction)
```

At this point, the next `ProcessInstanceAction` is added to the queue. 
Once the current `NodeInstanceTriggerAction.trigger()` method call has completed, the next
`NodeInstanceTriggerAction` is triggered.. 

```
RuleFlowProcessInstance.internalExecuteQueue()
NodeInstanceTriggerAction.trigger()
ActionNodeInstance(NodeInstanceImpl).trigger(NodeInstance, String)
ActionNodeInstance.internalTrigger(NodeInstance, String)
RuleFlowProcessInstance.addAfterInternalTriggerAction(ComplexInternalTriggerNodeInstance)
RuleFlowProcessInstance.addProcessInstanceAction(ProcessInstanceAction)
```

Again, the next `ProcessInstanceAction` is added to the queue halfway through the execution of the current one. 
While, the new `ProcessInstanceAction` is a `AfterInternalTriggerAction`, the idea is the same. 

```
RuleFlowProcessInstance.internalExecuteQueue()
AfterInternalTriggerAction.trigger()
ActionNodeInstance.afterInternalTrigger()
ActionNodeInstance.triggerCompleted()
ActionNodeInstance(NodeInstanceImpl).triggerCompleted(String, boolean)
ActionNodeInstance(NodeInstanceImpl).triggerNodeInstances(Deque<NodeInstanceTriggerAction>)
ActionNodeInstance(NodeInstanceImpl).triggerNodeInstance(NodeInstance, String)
ActionNodeInstance(NodeInstanceImpl).addNodeInstanceTrigger(NodeInstance, NodeInstance, String)
RuleFlowProcessInstance.addNodeInstanceTriggerAction(NodeInstance, NodeInstance, String)
RuleFlowProcessInstance.addProcessInstanceAction(ProcessInstanceAction)
```

The above patterns of execution continue until the process instance has completed or otherwise hit a save point. 

As you might suspect, there's a lot more logic in the process engine than simply triggering node instances -- and because all of that logic
is built around a recursive execution model, it also needs to split into smaller sized actions.

### Queue-based idioms 

One of the idioms that now occurs frequently in the code is this: 

```java
  boolean queueBased = isQueueBased();
  if( queueBased ) {
    // if queue-based, add action to do X (after Y)
    getProcessInstance().addAfterInternalTriggerAction(this); 
  }
  
  // do Y
  exceptionScopeInstance.handleException(faultName, processInstance.getFaultData()); 
  
  if( ! queueBased ) {
    // if NOT queue-based, do X
    afterInternalTrigger(); 
  }
```
   
In other words: 

1. If queue-based, add an action to do *`x`* to the queue to be executed **after we do** *`y`*.
2. Do *`y`*.
3. if *not* queue-based, do *`x`*.
                
Furthermore, in order to do this, some methods needed to be broken up into multiple methods. 

For examples, see `afterInternalTrigger()`, `afterEntryActions(NodeInstance, String)` and the
`afterExitActions(String, boolean)` (among others) the `NodeInstance` implementation classes. 

These methods are then called in various `ProcessInstanceAction` implementations. 

## Comparing queue-based event history to recursive event history

Let's take a simple process that contains the following nodes: 

    start ( node "A" ) --> script task ( node "B" ) --> end ( node "C" )
 
One simple way to see the difference between the recursive execution and the queue-based execution 
is to look at the history of events for this process. 

In the table below, we compare the two event histories. However, please first look at what the different abbreviations mean:

- `bnl` - before node leave
- `bnt` - before node trigger
- `ant` - after node trigger
- `anl` - after node leave
- `bps` - before process (instance) start
- `aps` - after process (instance) start
- `apc` - before process (instance) complete
- `apc` - after process (instance) complete
  
If we compare the event histories of the two execution modes for the above process, we see the following:

| Recursive | Queue Based |
| --------- | ----------- | 
| `bps`     | `bps`       |
| `bnt-A`   | `bnt-A`     |
| `bnl-A`   | `bnl-A`     |
| `bnt-B`   | **`ant-A`** |
| `bnl-B`   | **`anl-A`** |
| `bnt-C`   | `bnt-B`     |
| `bnl-C`   | `bnl-B`     |
| `bpc`     | **`anl-B`** |
| `apc`     | **`ant-B`** |
| `anl-C`   | `ant-A`     |
| `ant-C`   | `ant-A`     |
| `anl-B`   | **`ant-A`** |
| `ant-B`   | **`ant-A`** |
| `anl-A`   | `bpc`       |
| `ant-A`   | `apc`       |
| `aps`     | `aps`       |

While in recursive execution, the next node instance is triggered after `bnl-A`, `bnl-B` and `bnl-B`, in queue-based execution, 
these are the points at which the next `ProcessInstanceAction` is added to the queue.

The only event sequence that remains vaguely "recursive-like" in queue-based execution 
is the before- and after- process (instance) started (`bps`/`aps`) sequence. 
    
## Why don't we use the Drools `PropagationEntry`/`DefaultAgenda` mechanism for queue-based execution? 

(Primarily for Kris.. :smirk: )

There are actually 2 questions here: 

1. Why don't we just make each "`NodeInstance` action" a `PropagationEntry` instance so that the `DefaultAgenda` can then execute it? 
2. Okay, what about making the *entire* process instance a `PropagtionEntry`?

There are a couple of things going on: 

* Because of process-specific mechanisms (like subprocesses, multi-instance processes and exception scopes and handling), 
  we need to be able to *scope* actions or otherwise have fine-grained control over the order in which actions added 
  to the queue are executed.
  * However, the `DefaultAgenda` mechanism does not give us this: it's (current) architecture is optimized for 
    performant rules evaluation and execution, which does not fit our needs. 
  * For example, when an event is signalled, we need to make sure that any actions that are already on the queued are only
    executed *after all of the actions caused by the signalled event complete*. This is because the logic in the process engine
    assumes a "recursive" execution, where the signalled event "method" will fully complete and return before proceeding. 
    
* If a node instance or process instance is executed as a `PropagationEntry`, and that `PropagationEntry` is executed, *then 
  no other `PropagationEntry` can be executed*. 
  * This poses a problem for processes that rely on rules, because the current functionality assumes that a rule-task or other operation on a `KieSession` is executed immediately. 
  * However, if the `DefaultAgenda` is already execuing the node instance or process instance execution, then any new operation is *queued* (in a `PropagationList`)
    and will only be executed *after* the current action. 
    
For these reasons, the queue-based execution has remained entirely within the jBPM code base. The added benefit is that it also
does not further complication any separation of code concerns between Drools and JBPM.

## Queue-based execution and performance

- large splits with lots of subprocesses (100 branches) -- slower, scoping actions means list creation... 
- large loops -- possible and faster

- additionally: now possible to "goto"... :D
- compensation... 

## Challenges posed by using Queue-Based actions in logic (code) that assumes recursive execution

### Performance

For several reasons, the Queue-based implementation requires that more objects are created. The
primary causes for this are the following: 

1. Because we're serially executing the queue (instead of recursively executing node instances), 
  the objects we fill the queue with need to have similar or uniform design. This uniform design 
  lets us execute each object we retrieve from the queue easily. 
  - In other words, we're filling the queue with `ProcessInstanceAction` objects. Because
    each `ProcessInstanceAction` implementation implements a `trigger()` and `actsOn(...)`
    method, the queue-based algorithm doesn't have to pay attention to what type of action is 
    actually being executed -- however, this means that the cost of this simplification is that
    the actual `NodeInstance` (or other object) being executed is always wrapped in a
    `ProcessInstanceAction` implementation. 
  - This means that we are creating more objects (`ProcessInstanceAction` implementations) than for 
    recursive based execution.
2. Furthermore, because we need to be able to scope execution, and this takes place in 

  
a similar method



- removing/cancelling node instances
- aborting process instances
- exceptions
- events
- workitems


--------------------
When implementing an algorithm, regardless of the language, one of the choices made is between a
functional approach and an data-based or object-oriented approach. 

All algorithms have to take a certain amount of "state" into account. If we look at a simple 
algorithm that approximates the total area of a geometric shape (a triangle, for example), there
are 2 types of state here: 
1. The actual result of the calculation: the total area
2. Where we are in the execution of algorithm

The process engine, if looked at as an algorithm, is *not* concerned with the 1rst type of state: in
fact, it's an algorithm designed specifically to track but not be aware of the result of a process
execution. 

However, we still have the second type of "state" in the process engine: where we are in the
execution of the code. 

Let's go back to our triangle area calculation example: let's say we calculate the area like this: 

1. Divide the triangle into equally thin columns. 
2. For each column: 
   a. get the length of the column
   b. calculate the area of of the column (length x width)
   c. add the column area to the result

(Yes, this is what Newton did with integral calculus.. ;) )

The algorithmic state in this algorithm is of course  *which* column we're calculating. For example, 
if we write pseudo-code for the above algorithm: 

```Java
int result = 0;
int colWidth = 1;
int numCol = divideShapeIntoColumns(shape, colWidht);
for( int i = 0; i < numCol; ++i ) { 
  int h = getCol(i); 
  int area = h * colWidth;
  result += area;
}
return result;
```

See `i`? That's our algorithmic state! However, we can of course write this algorithm recursively,
right? 





