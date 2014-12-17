/*
 * CS542 project: Simulate Link-State Routing Algorithm
 * Compute the Least Cost between any Two Routers
*/

//Libraries used
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Panel;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.Scanner;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.filechooser.FileNameExtensionFilter;

public class LinkStateRoutingSimulator extends JFrame implements ActionListener
{
    Panel header, footer, center, content, output;
    JLabel l1, l2, l3, l4, l5;
    JTextArea result = new JTextArea();
    JTextField src_router, dest_router;
    JButton inputFile, connTable, shortPath, clear;
    JFileChooser chooser;
    FileNameExtensionFilter filter;
    int[][] inputRoute;
    int[][] connRoute;
    int routerCount = 0;
    int srcRouter = 0, src = 0,destRouter = 0, dist = 0, flag = 0, cflag = 0, sflag = 0;
    int router;
    String color;
    int node_dist;
    LinkStateRoutingSimulator pred;
    ArrayList<LinkStateRoutingSimulator> queue;
    ArrayList<LinkStateRoutingSimulator> black;
    ArrayList<LinkStateRoutingSimulator> compute_queue;
    ArrayList<Integer> index_result;
    ArrayList<String> shortestRoute;
    
    //Constructor method to define Router Properties
    public LinkStateRoutingSimulator(int r, String clr, int d, LinkStateRoutingSimulator j)
    {
        router = r; //Router
        color = clr; //Color
        node_dist = d; //distance
        pred = j; //Predecessor
    }
    //Constructor method to define GUI
    public LinkStateRoutingSimulator()
    {
        // Header Panel: Contains the name of the Application
        header=new Panel();
        l1 = new JLabel("CS542 Link-State Routing Simulator",JLabel.CENTER);
        l1.setFont(new Font("Serif", Font.BOLD, 30));
        l1.setForeground(Color.DARK_GRAY);
        header.add(l1);
        
        //Center Panel: Contains the functionality of the application
        center =new Panel();
        center.setLayout(new BorderLayout());
        //Contetn Panel: Containing the application interface functionality
        content = new Panel();
        content.setLayout(new GridLayout(0,3));
        
        l2 = new JLabel("Open a Network Topology File...",JLabel.CENTER);
        l2.setFont(new Font("Serif", Font.PLAIN, 15));
        inputFile = new JButton("Open");
        chooser = new JFileChooser();
        filter = new FileNameExtensionFilter("text only","txt");
        chooser.setFileFilter(filter);
        
        l3 = new JLabel("Build a Connection Table...",JLabel.CENTER);
        l3.setFont(new Font("Serif", Font.PLAIN, 15));
        src_router = new JTextField(10);
        src_router.setBackground(Color.white);
        connTable = new JButton("Build");
        
        l4 = new JLabel("Shortest Path to Destination Router...",JLabel.CENTER);
        l4.setFont(new Font("Serif", Font.PLAIN, 15));
        dest_router = new JTextField(10);
        dest_router.setBackground(Color.white);
        shortPath = new JButton("Shortest Path");
        
        l5 = new JLabel("Reset All...",JLabel.CENTER);
        l4.setFont(new Font("Serif", Font.PLAIN, 15));
        clear = new JButton("Reset All");
        
        content.add(l2);
        content.add(new JLabel("",JLabel.CENTER));
        content.add(inputFile);
        
        content.add(l3);
        content.add(src_router);
        content.add(connTable);
        
        content.add(l4);
        content.add(dest_router);
        content.add(shortPath);
        
        content.add(l5);
        content.add(new JLabel("",JLabel.CENTER));
        content.add(clear);
        
        content.add(new JLabel("",JLabel.CENTER));
        content.add(new JLabel("",JLabel.CENTER));
        content.add(new JLabel("",JLabel.CENTER));
        
        //Output Panel: Contains the Result
        output=new Panel();
        result = new JTextArea(50,50);
        result.setEditable(false);
        output.add(result);

        center.add("North",content);
        center.add("Center",output);
        
        //Footer Panel: Contains the credits information
        footer=new Panel();
        l1= new JLabel("Credits: Alaa Alali, Arihant Raj Nagarajan, Rahul Krishnamurthy",JLabel.CENTER);
        l1.setFont(new Font("Serif", Font.BOLD + Font.ITALIC, 20));
        l1.setForeground(Color.DARK_GRAY);
        footer.add(l1);
        
        //Implementing Action Listener to the GUI interface to invoke the respective function
        inputFile.addActionListener(this); //Used to Read the Input from the User
        connTable.addActionListener(this); //Create Connection Table based on Source Router
        shortPath.addActionListener(this); //Create Shortest Path based on Destination Router
        clear.addActionListener(this); //Reset all the fields entered
        
        //Setting Application Frame Layout and Adding the Panels to the Frame
        setLayout(new BorderLayout(50,50));
        add("North",header);
        add("Center",center);
        add("South",footer);
        setTitle("Link-State Routing Simulator");
        setSize(1000,650);
        setLocation(200,10);
        setVisible(true);
        
        //Window Listener used to respond to close the Application
        addWindowListener(new WindowAdapter()
        {
            public void windowClosing(WindowEvent we)
            {
                System.exit(0);
            }
        });
    }
    
    //Action Performed: Used to identify which button is clicked and perform the correct action
    public void actionPerformed(ActionEvent  ae)
    {
        // Action: Read Input
        if(ae.getSource() == inputFile)
        {
            int returnVal = chooser.showOpenDialog(LinkStateRoutingSimulator.this);
            if (returnVal == JFileChooser.APPROVE_OPTION)
            {
                File file = chooser.getSelectedFile(); //GUI for File Chooser
                if(flag == 0)
                {
                    result.setForeground(Color.BLUE);
                    result.setText("Opening: "+ file.getName() + "\n\n");
                    result.append("Review original topology matrix:\n\n");
                    reviewTopology(file); //Reviewing the Topology provided
                }
                else
                {
                    result.setForeground(Color.RED);
                    result.setText("Network Topology already submitted ...\n\nPlease Clear the Topology and then submit a New Topology");
                }
            }
            else
            {
                result.setForeground(Color.RED);
                result.setText("File Could not be opened");
            }
        }
        // Action: Prepare Connection Table for the given source
        if(ae.getSource() == connTable)
        {
            if(cflag == 0)
            {
                if(inputRoute != null)
                {
                    if(src_router.getText().length() > 0)
                    {
                        try
                        {
                            if(Integer.parseInt(src_router.getText()) > 0 && Integer.parseInt(src_router.getText()) <= routerCount)
                            {
                                // Obtaining the Source Router from User
                                srcRouter = Integer.parseInt(src_router.getText());
                                src = srcRouter;
                                result.setForeground(Color.BLUE);
                                connectionTable();
                                cflag = 1;
                                
                                // Displaying the Connection Table Result
                                result.setText("Router "+ srcRouter +" Connection Table\n\n");
                                result.append("Destination\tInterface\n");
                                result.append("----------------\t------------\n");
                                for(int i =0; i < routerCount; i++)
                                {
                                    result.append("          " + (i+1) + "\t      ");
                                    if(connRoute[i][0] == 0)
                                        result.append("--\n");
                                    else
                                        result.append(connRoute[i][0] +"\n");
                                }
                            }
                            else
                            {
                                result.setForeground(Color.RED);
                                result.setText("Error!!\n\nNo Router Found..");
                            }
                        }
                        catch(Exception e)
                        {
                            result.setForeground(Color.RED);
                            result.setText("Error!!\n\nNo Router Found..");
                        }
                    }
                    else
                    {
                        result.setForeground(Color.RED);
                        result.setText("Error!!\n\nEnter Source Router and try again");
                    }
                }
                else
                {
                    result.setForeground(Color.RED);
                    result.setText("Error!!\n\nSelect Network Topology File");
                }
            }
            else
            {
                result.setForeground(Color.RED);
                result.setText("Error!!\n\nConnection Table already generated...");
            }
        }
        // Action: Generate Shortest Path
        if(ae.getSource() == shortPath)
        {
            if(sflag == 0)
            {
                if(inputRoute != null)
                {
                    try
                    {
                        if(src > 0)
                        {
                            if(dest_router.getText().length() > 0)
                            {
                                if(Integer.parseInt(dest_router.getText()) > 0 && Integer.parseInt(dest_router.getText()) <= routerCount)
                                {
                                    // Obtaining the Destination Router from user
                                    destRouter = Integer.parseInt(dest_router.getText());
                                    result.setForeground(Color.BLUE);
                                    result.setText("The Shortest Path from Router "+ srcRouter +" to Router "+ destRouter +" is: \n\n");
                                    shortestPath();
                                    sflag = 1;
                                }
                                else
                                {
                                    result.setForeground(Color.RED);
                                    result.setText("Error!!\n\nNo Router Found...");
                                }                
                            }
                            else
                            {
                                result.setForeground(Color.RED);
                                result.setText("Error!!\n\nEnter Destination Router and try again");
                            }
                        }
                        else
                        {
                            result.setForeground(Color.RED);
                            result.setText("Error!!\n\nEnter Source Router and try again");
                        }
                    }
                    catch(Exception e)
                    {
                        result.setForeground(Color.RED);
                        result.setText("Error!!\n\nEnter Valid Input..");
                    }
                }
                else
                {
                    result.setForeground(Color.RED);
                    result.setText("Error!!\n\nSelect Network Topology File");
                }
            }
            else
            {
                result.setForeground(Color.RED);
                result.setText("Error!!\n\nShortest Path already generated...");
            }
        }
        // Action: Reset All Inputs
        if(ae.getSource() == clear)
        {
            if(flag != 0)
            {
            inputRoute = null;
            connRoute = null;
            shortestRoute = null;
            queue = null;
            black = null;
            compute_queue = null;
            index_result = null;
            routerCount = 0; srcRouter = 0; src = 0; destRouter = 0; dist = 0; flag = 0; cflag = 0; sflag = 0;
            result.setForeground(Color.BLUE);
            result.setText("Topology has been cleared...\n\nProceed with the new Topology");
            }
            else
            {
                result.setForeground(Color.RED);
                result.setText("No Topology present...\n\nContinue with the Task");
            }
        }
    }
    
    //Reviewing the entered Topology
    public void reviewTopology(File readFile)
    {
        try
        {
            Scanner fileInput = new Scanner(readFile);
            StringBuilder sb = new StringBuilder();
            String data = null;
            flag = 1;
            //Determining the number of routers given in the Topology
            while (fileInput.hasNextLine())
            {
                String line = fileInput.nextLine();
                routerCount++;
            }
            fileInput = new Scanner(readFile);
            inputRoute = new int[routerCount][routerCount];
            int row = 0;
            //Reading the input topology and generating the Routing Table
            while (fileInput.hasNextLine())
            {
                int column =0;
                String line = fileInput.nextLine();
                line = line.trim();
                Scanner readLine = new Scanner(line);
                while(readLine.hasNextLine())
                {
                    String word = readLine.next();
                    result.append(word+"     ");
                    inputRoute[row][column] = Integer.parseInt(word);
                    ++column;
                }
                result.append("\n");
                row++;
            }
        }
        catch(Exception e)
        {
            inputRoute = null;
            flag = 0;
            result.setForeground(Color.RED);
            result.setText("Error!!\n\nInvalid Topology...");
        }
        
    }
    
    //Prepar Connection Table based on First Hop
    public void connectionTable()
    {
        queue = new ArrayList<LinkStateRoutingSimulator>();
        black = new ArrayList<LinkStateRoutingSimulator>();
        compute_queue = new ArrayList<LinkStateRoutingSimulator>();
        connRoute = new int[routerCount][1];
        LinkStateRoutingSimulator s = new LinkStateRoutingSimulator((src-1),"GRAY",(inputRoute[src-1][src-1]),null);
        queue.add(s);
        BFS();
        genConnectionTable();
    }
    
    //BFS Algorithm to determine shortest path
    public void BFS()
    {
        int flag = 0;
        for(int c = 0; c<queue.size(); c++)
        {
            String cl = checkProcessed((queue.get(c)).router);
            if(cl.equals("WHITE"))
            {
                for(int i = 0; i < routerCount; i++)
                {
                    if(queue.get(c).router != i)
                    {
                        if(inputRoute[(queue.get(c)).router][i] != -1)
                        {
                            LinkStateRoutingSimulator node = new LinkStateRoutingSimulator(i,"GRAY",((queue.get(c)).node_dist + inputRoute[((queue.get(c)).router)][i]),queue.get(c));
                            queue.add(node);
                        }
                    }
                }
                queue.get(0).color = "BLACK";
                black.add(queue.get(c));
            }
        }
        compute_queue = queue;
    }

    //Obtaining the Minimum Path Length
    public int minVal(ArrayList<Integer> index, int min)
    {
        int pos = 0;
        for(int j = 0; j < (index).size(); j++)
        {
            if(min >= (compute_queue.get(index.get(j))).node_dist)
            {
                min = (compute_queue.get(index.get(j))).node_dist;
                pos = compute_queue.indexOf(compute_queue.get(index.get(j)));
            }
        }
        return pos;
    }
    
    //Obtaining the Index position
    public void getIndex(int i)
    {
        index_result = new ArrayList<Integer>();
        for(int j = 0; j < compute_queue.size(); j++)
        {
            if((compute_queue.get(j)).router == i)
            {
                index_result.add(j);
            }
        }
    }
    
    //Connection table calculation
    public void genConnectionTable()
    {
        ArrayList<LinkStateRoutingSimulator> result;
        for(int i = 0 ; i < routerCount; i++)
        {
            result = new ArrayList<LinkStateRoutingSimulator>();
            getIndex(i);
            int min = (compute_queue.get(index_result.get(0))).node_dist;
            int position = minVal(index_result, min);
            int jump = position;
            if((compute_queue.get(jump)).pred != null)
            {
                while(((compute_queue.get(jump)).pred).router != (src-1))
                {
                    jump = ((compute_queue.get(jump)).pred).router;
                    getIndex(jump);
                    jump = minVal(index_result, min);
                }
                connRoute[i][0] = (compute_queue.get(jump)).router + 1;
            }
            else
                connRoute[i][0] = 0;
        }
    }
    
    //Memoization of the routers
    public String checkProcessed(int testr)
    {
        int flag = 0;
        //Checking the node with Black nodes
        if(!black.isEmpty())
        {
            for(int i = 0; i < black.size(); i++)
            {
                if(testr == ((black.get(i)).router) )
                    flag = 1;
            }
        }
        if(flag != 1)
            return("WHITE");
        else
            return("BLACK");
    }

    //Shortest Path from Source to Destination
    public void shortestPath()
    {
        shortestRoute = new ArrayList<String>();
        getIndex(destRouter-1);
        int min = (compute_queue.get(index_result.get(0))).node_dist;
        int position = minVal(index_result, min);
        int jump = position;
        dist = 0;
        shortestRoute.add(Integer.toString(((compute_queue.get(jump)).router)+1));
        if((compute_queue.get(jump)).pred != null)
        {
            while(((compute_queue.get(jump)).pred).router != (src-1))
            {
                jump = ((compute_queue.get(jump)).pred).router;
                getIndex(jump);
                jump = minVal(index_result, min);
                shortestRoute.add(Integer.toString(((compute_queue.get(jump)).router)+1));
            }
            shortestRoute.add(Integer.toString((((compute_queue.get(jump)).pred).router)+1));
        }
        else
            shortestRoute.add(Integer.toString(((compute_queue.get(jump)).router)+1));

        if(srcRouter == destRouter)
        {
            result.append(srcRouter +" --> "+ destRouter);
            dist = inputRoute[srcRouter-1][destRouter-1];
        }
        else
        {
            for(int i = shortestRoute.size() - 1; i >=0 ; i--)
            {
                result.append(shortestRoute.get(i));
                if(i != 0)
                {
                    result.append(" -> ");
                    dist = dist + inputRoute[(Integer.parseInt(shortestRoute.get(i))-1)][(Integer.parseInt(shortestRoute.get(i-1))-1)];
                }
            }
        }

        result.append("\n\n");
        result.append("The total cost is : "+ dist);
    }
    
    public static void main(String[] args) 
    {
        //Generating GUI Interface
        new LinkStateRoutingSimulator();
    }
}
