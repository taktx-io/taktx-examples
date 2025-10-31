This is an example Docker Compose setup for running a full TaktX environment with all core services.
This docker compose file can be used to quickly spin up a local or test environment for TaktX.

It includes the following services:
- `taktx` — The main TaktX backend service
- `kafka` - A single node kafka broker
- `kafka-ui` - Kafka UI for managing and inspecting Kafka topics
- `Grafana` - Grafana for metrics visualization
- `Prometheus` - Prometheus for metrics collection

## Prerequisites
- Docker and Docker Compose installed on your machine.
- License file for TaktX engine. You can obtain a license by contacting TaktX via their [website](https://taktx.io/contact).
- Sufficient system resources to run multiple containers.
- (Optional) Basic knowledge of Docker and Docker Compose.

## Usage
1. Clone this repository or copy the contents of this directory to your local machine.`
2. Obtain a license file for TaktX via the [Taktx Website](https://taktx.io/contact). By default the docker compose file is configured to look for the license file in  USER_HOME/.taktx/license.lic. Adapt the docker compose file if necessary.
3. Navigate to the directory containing the `docker-compose-full.yml` file.
3. Run the following command to start all services:
   ```bash
   docker compose -f docker-compose-full.yml up -d
   ```
4. Wait for all services to start. You can check the status of the containers using:
   ```bash
   docker compose -f docker-compose-full.yml ps
   ```
5. Access the services:
   - TaktX Core: `http://localhost:8080`: The main TaktX engine. Currently it provides a basic swagger UI 
   - Kafka UI: `http://localhost:8085: For topic monitoring and management.
   - Grafana: `http://localhost:3000`: A default dashboard is pre-configured to visualize TaktX metrics.
   - Prometheus: `http://localhost:9090`: For metrics querying.
   - Kafka Broker: `http://localhost:9092`: The Kafka broker endpoint for connecting clients.

6. To stop and remove the containers, run:
   ```bash
   docker compose -f docker-compose-full.yml down
   ```

## Configuration    
You can customize the configuration of each service by modifying the 
`docker-compose-full.yml` file. For example, you can change port mappings, 
environment variables, and volume mounts as needed.

## Notes
No authentication is configured for the services in this setup. For production use,
consider adding appropriate security measures.
Feel free to extend this setup by adding more services or modifying existing ones to suit your needs.

## Troubleshooting
If you encounter issues while starting the services, check the logs of individual containers using:
```bash
docker-compose -f docker-compose-full.yml logs <service_name>
```
Replace `<service_name>` with the name of the service you want to inspect (e.g., `taktx`, `kafka`, etc.).
