import run_agent
import numpy as np
import pickle

all_agents = ["random", "naive"]
numtrials = 1000
rom = 'space_invaders.bin'

def run_trials(agents, gui=False):
    agent_rewards = {}

    for agent in agents:
        print "Collecting data for agent: " + agent

        agent_rewards[agent] = run_agent.run_agent(agent=agent, gui=gui, max_episodes=numtrials, rom=rom)

    print "All results:"
    for agent in agents:
        print agent + ":"
        rewards = np.array(agent_rewards[agent])
        print "average reward: " + str(np.average(rewards))
        print "average std dev: " + str(np.std(rewards))

        print

    output = open('trial_data.pkl', 'wb')
    pickle.dump(agent_rewards, output)

if __name__ == '__main__':

    import argparse

    parser = argparse.ArgumentParser()
    parser.add_argument("--agent", default='all',
                        help="the name of the agent to use (all lowercase) or 'all' to go through all agents")
    parser.add_argument("-g", "--gui", action="store_true",
                        help="flag to turn on gui")

    args = parser.parse_args()
    if (args.agent == 'all'):
        agents = all_agents
    else:
        agents = [args.agent]
    gui = args.gui

    run_trials(agents, gui=gui)
