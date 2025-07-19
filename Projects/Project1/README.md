# SD practical assignment

## Shared Data & Concurrency
This project simulates a voting system using Java threads, semaphores, and locks to manage shared data. The main components include:
- **Voter**: Represents a person who votes and may answer a questionnaire.
- **Clerk**: Validates if a voter has already voted and processes their request.
- **Pollster**: Conducts interviews with voters and aggregates their responses.
- **GUI**: Provides a graphical interface for the simulation, displaying real-time updates on voting status and results.
- **DataStruct**: Contains shared data structures and synchronization mechanisms (semaphores and locks) to ensure thread safety.
- **VoterIDGen**: Generates unique IDs for each voter, ensuring no duplicates.
- **Main**: The entry point of the application, which initializes the GUI and starts the simulation.

## Voter
Each instance of a voter (implemented in Voter.java) simulates a voter entering a queue, waiting for a clerk, and casting a vote. After voting, the voter may also answer a questionnaire to provide feedback. Voter IDs are generated via VoterIDGen.java which safely increments an ID counter using a lock. Voters also “rebirth” (restart themselves), and they may change the Voter ID based on a probability.

## Clerk
Clerk threads (in Clerk.java) are responsible for validating if a voter has already voted. They wait for requests signaled from voters, check eligibility (using shared semaphores and mutual exclusion in DataStruct.java), and then respond with the result.

## Pollster
The pollsters (in Pollster.java) intercept questionnaire requests from voters. They decide whether to conduct the interview and record the response. Their responses are then aggregated into vote counts for further evaluation.

## Workflow
When the user starts the simulation via the GUI, threads for voters, clerks, and pollsters are created and started. The threads coordinate via semaphores and locks to ensure the right order of operations (such as checking voting status, updating vote counts, and processing questionnaire answers). The simulation stops after a specified time, aggregates the results, and prints out the final counts.