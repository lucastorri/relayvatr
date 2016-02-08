# Relayvatr

An Elevator Control System.

## Overview

On each iteration, an elevator can be performing one of the following operations:

  * Being idle;
  * Passing through a floor without stopping;
  * Arriving at a specific floor;
  * Leaving the floor to continue its trip.

Users can leave or enter an elevator car only when the elevator arrives at a floor. They also interact with the system in two ways:

* Requesting an elevator at their current floor, and giving a direction that they want to travel (up or down);
* Pressing a floor button when inside an elevator.

The behaviour determining which elevator will handle a given call and how an elevator will move between floors are described respectively by two classes:

1. `ClosestElevatorScheduler`;
2. `SameDirectionElevator`.

Each elevator available is mapped to an instance of `SameDirectionElevator`. On each iteration of the system, the `ClosestElevatorScheduler` asks each elevator to evaluate how well they can handle each of the open user calls. Finally, it decides the ones that seem to be best for each call. If a call cannot be handled straight away, a new attempt will be made on the next iteration.

Briefly described, the elevators decide if they can handle a user call using the algorithm described below.

Implementation details can be found on the ScalaDoc of each class.

### Algorithm

The algorithm implemented here is modeled after the one described [on Wikipedia](https://en.wikipedia.org/wiki/Elevator#The_elevator_algorithm).

If a user called an elevator, where the user is:

  *  on an upper floor:
     - and the user wants to go down: will go straight and only when descending get others on the way down;
     - and the user wants to go up: will collect anyone on the way that is going up;
  * on an lower floor:
     - and the user wants to go down: will collect anyone on the way that is going down;
     - and the user wants to go up: will go straight and only when ascending get others on the way up;
  * already on the same floor:
     - will go on the direction requested by the first user, picking up users that are going on the same direction.


## Building and Running

Operations can be performed with the `sbt` script available on the source's directory.

### Dependencies

* Java 8

### Running

The following command will run a simulated version of the system, where random user trips will be generated and applied to it. The parameters used on the simulation can be seen on the class `relayvatr.Main`.

```bash
./sbt run
```

### Test

This command will run all the unit tests available:

```bash
./sbt test
```

### ScalaDoc

After running the following command, the index page will be available at `target/scala-2.11/api/index.html`.

```bash
./sbt doc
```
