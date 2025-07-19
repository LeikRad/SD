#!/bin/bash

# Compile the code
make all

# Open a new terminal window for each server
echo "Starting PollingStationServer..."
gnome-terminal --title="PollingStationServer" -- bash -c "java -cp build/output.jar sd.main.serverSide.entities.PollingStationServer; exec bash"
sleep 1

echo "Starting IDCheckServer..."
gnome-terminal --title="IDCheckServer" -- bash -c "java -cp build/output.jar sd.main.serverSide.entities.IDCheckServer; exec bash"
sleep 1

echo "Starting PollsterServer..."
gnome-terminal --title="PollsterServer" -- bash -c "java -cp build/output.jar sd.main.serverSide.entities.PollsterServer; exec bash"
sleep 1

echo "Starting VotingBoothServer..."
gnome-terminal --title="VotingBoothServer" -- bash -c "java -cp build/output.jar sd.main.serverSide.entities.VotingBoothServer; exec bash"
sleep 1

# Give servers a few seconds to fully start

echo "Starting Clerk..."
gnome-terminal --title="Clerk" -- bash -c "java -cp build/output.jar sd.main.clientSide.entities.ClerkClient; exec bash"
sleep 1

echo "Starting Pollster..."
gnome-terminal --title="Pollster" -- bash -c "java -cp build/output.jar sd.main.clientSide.entities.PollsterClient; exec bash"
sleep 1

# Open a new terminal window for the client.
echo "Starting Voter..."
gnome-terminal --title="Voter" -- bash -c "java -cp build/output.jar sd.main.clientSide.entities.VoterClient; exec bash"