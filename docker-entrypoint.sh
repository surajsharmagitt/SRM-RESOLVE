#!/bin/bash
set -e

echo "=== Initializing SRM-RESOLVE Container Environment ==="

# Check if an external SPRING_DATASOURCE_URL is already provided
if [ -n "$SPRING_DATASOURCE_URL" ] && [[ "$SPRING_DATASOURCE_URL" != *"localhost"* ]]; then
    echo "External database URL detected ($SPRING_DATASOURCE_URL). Skipping local MariaDB initialization."
else
    echo "Starting internal MariaDB service..."
    mkdir -p /var/run/mysqld /var/lib/mysql
    chown -R mysql:mysql /var/run/mysqld /var/lib/mysql

    # Initialize datadir if empty
    if [ ! -d "/var/lib/mysql/mysql" ]; then
        echo "Initializing MariaDB data directory..."
        mysql_install_db --user=mysql --datadir=/var/lib/mysql > /dev/null 2>&1 || true
    fi

    # Start MariaDB daemon in the background
    /usr/bin/mysqld_safe --datadir=/var/lib/mysql --skip-syslog &

    # Wait for MariaDB to become ready
    echo "Waiting for MariaDB to be ready on port 3306..."
    for i in {1..30}; do
        if mysqladmin ping --silent 2>/dev/null; then
            echo "MariaDB is ready and listening on localhost:3306."
            break
        fi
        sleep 1
    done

    # Create srm_resolve database and grant root permissions without password
    echo "Setting up srm_resolve database..."
    mysql -e "CREATE DATABASE IF NOT EXISTS srm_resolve CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;" || true
    mysql -e "ALTER USER 'root'@'localhost' IDENTIFIED VIA mysql_native_password USING PASSWORD('');" || true
    mysql -e "GRANT ALL PRIVILEGES ON *.* TO 'root'@'localhost' WITH GRANT OPTION;" || true
    mysql -e "FLUSH PRIVILEGES;" || true
    echo "Database ready."
fi

echo "=== Launching SRM-RESOLVE Spring Boot Application on PORT ${PORT:-8080} ==="
exec java -Duser.timezone=Asia/Kolkata -Xmx320m -Xms128m -Dserver.port=${PORT:-8080} -jar app.jar

